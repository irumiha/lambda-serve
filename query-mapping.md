# Query Parameter and Form Data Mapping

## Overview

The `mapextract` module provides a type-safe mechanism for mapping HTTP query parameters and form submissions to Scala case classes. It uses compile-time derivation via Magnolia to automatically generate mapping logic for case classes, eliminating the need for manual parsing and validation code.

## Naming Scheme

### Flat Parameters

For simple scalar fields, the parameter name matches the case class field name:

```scala
case class User(name: String, age: Int) derives MapExtract

// Maps from:
// name=John&age=30
```

### Nested Objects

Nested case classes use dot notation to represent hierarchy:

```scala
case class Address(street: String, city: String) derives MapExtract
case class Person(name: String, address: Address) derives MapExtract

// Maps from:
// name=John&address.street=Main St&address.city=NYC
```

### Collections (Lists, Vectors, Sequences)

Collections are represented by repeating the same parameter name with multiple values:

```scala
case class Form(tags: List[String]) derives MapExtract

// Maps from:
// tags=scala&tags=http&tags=server
```

### Nested Collections

When a field is a collection of objects, there are two supported formats:

#### Indexed Notation (Recommended)

Use explicit array-style indices to specify which element each field belongs to:

```scala
case class Pet(name: String, kind: String) derives MapExtract
case class Owner(name: String, pets: List[Pet]) derives MapExtract

// Maps from:
// name=John&pets[0].name=Max&pets[0].kind=Dog&pets[1].name=Milly&pets[1].kind=Cat
//
// Results in:
// Owner("John", List(Pet("Max", "Dog"), Pet("Milly", "Cat")))
```

This format is more explicit and allows for:
- **Non-sequential indices**: `items[0]`, `items[2]`, `items[5]` (gaps are allowed)
- **Clearer semantics**: Each object's fields are explicitly grouped by index
- **Better error detection**: Mismatched indices are easier to identify

#### Index-Aligned Format (Legacy, Backward Compatible)

Use dot notation with repeated values. All elements at the same index are grouped together:

```scala
case class Pet(name: String, kind: String) derives MapExtract
case class Owner(name: String, pets: List[Pet]) derives MapExtract

// Maps from:
// name=John&pets.name=Max&pets.name=Milly&pets.kind=Dog&pets.kind=Cat
//
// Results in:
// Owner("John", List(Pet("Max", "Dog"), Pet("Milly", "Cat")))
```

Using this approach the collections are **index-aligned**: the first value of `pets.name` corresponds with the first value of `pets.kind`, and so on. This format exists primarily to support parsing HTTP headers with multiple values into case classes.

### Optional Fields

Optional fields can be represented using `Option[T]`:

```scala
case class SearchQuery(term: String, filter: Option[String]) derives MapExtract

// Maps from either:
// term=scala (filter will be None)
// term=scala&filter=recent (filter will be Some("recent"))
```

### Enums

Scala 3 enums can be mapped by matching the enum case name as a string:

```scala
enum Status derives MapExtract:
  case Active
  case Inactive
  case Pending

case class Account(id: String, status: Status) derives MapExtract

// Maps from:
// id=123&status=Active
```

### Custom Field Names

Use the `@SourceName` annotation to map a different query parameter name to a field:

```scala
case class User(
  @SourceName("user_name") name: String,
  age: Int
) derives MapExtract

// Maps from:
// user_name=John&age=30
```

## Supported Types

### Primitive Types
- `String`
- `Short`, `Int`, `Long`
- `Float`, `Double`, `BigDecimal`
- `Boolean`
- `UUID`

### Time Types
- `LocalDate`
- `LocalDateTime`
- `ZonedDateTime`
- `Instant`

### Container Types
- `Seq[T]`, `List[T]`, `Vector[T]`
- `Option[T]`

### Derived Types
- Case classes (with `derives MapExtract`)
- Enums (with `derives MapExtract`)

## Usage Examples

### HTTP GET Query Parameters

```scala
case class SearchRequest(
  query: String,
  page: Int,
  tags: List[String]
) derives MapExtract

// From query string: ?query=scala&page=1&tags=web&tags=http
val params = Map(
  "query" -> IndexedSeq("scala"),
  "page" -> IndexedSeq("1"),
  "tags" -> IndexedSeq("web", "http")
)

val request = summon[MapExtract[SearchRequest]].projectMap(params)
// SearchRequest("scala", 1, List("web", "http"))
```

### HTML Form Submission

```scala
case class ContactForm(
  name: String,
  email: String,
  message: String,
  subscribe: Boolean
) derives MapExtract

// From form POST data
val formData = Map(
  "name" -> IndexedSeq("Jane Doe"),
  "email" -> IndexedSeq("jane@example.com"),
  "message" -> IndexedSeq("Hello!"),
  "subscribe" -> IndexedSeq("true")
)

val form = summon[MapExtract[ContactForm]].projectMap(formData)
```

### Complex Nested Forms

#### Using Indexed Notation

```scala
case class Address(street: String, city: String, zip: String) derives MapExtract
case class Phone(countryCode: String, number: String) derives MapExtract

case class RegistrationForm(
  firstName: String,
  lastName: String,
  addresses: List[Address],
  phones: List[Phone]
) derives MapExtract

val formData = Map(
  "firstName" -> IndexedSeq("John"),
  "lastName" -> IndexedSeq("Smith"),
  "addresses[0].street" -> IndexedSeq("123 Main St"),
  "addresses[0].city" -> IndexedSeq("NYC"),
  "addresses[0].zip" -> IndexedSeq("10001"),
  "addresses[1].street" -> IndexedSeq("456 Oak Ave"),
  "addresses[1].city" -> IndexedSeq("Boston"),
  "addresses[1].zip" -> IndexedSeq("02101"),
  "phones[0].countryCode" -> IndexedSeq("+1"),
  "phones[0].number" -> IndexedSeq("5551234"),
  "phones[1].countryCode" -> IndexedSeq("+1"),
  "phones[1].number" -> IndexedSeq("5555678")
)

val registration = summon[MapExtract[RegistrationForm]].projectMap(formData)
// RegistrationForm(
//   "John", "Smith",
//   List(Address("123 Main St", "NYC", "10001"), Address("456 Oak Ave", "Boston", "02101")),
//   List(Phone("+1", "5551234"), Phone("+1", "5555678"))
// )
```

#### Using Index-Aligned Format (Legacy)

```scala
val formData = Map(
  "firstName" -> IndexedSeq("John"),
  "lastName" -> IndexedSeq("Smith"),
  "addresses.street" -> IndexedSeq("123 Main St", "456 Oak Ave"),
  "addresses.city" -> IndexedSeq("NYC", "Boston"),
  "addresses.zip" -> IndexedSeq("10001", "02101"),
  "phones.countryCode" -> IndexedSeq("+1", "+1"),
  "phones.number" -> IndexedSeq("5551234", "5555678")
)

val registration = summon[MapExtract[RegistrationForm]].projectMap(formData)
// Same result as above
```

## Error Handling

The mapper throws `IllegalArgumentException` in two cases:

1. **Missing required field**: When a non-optional field is not present in the parameter map
   ```scala
   // Throws: "Field not found at path fieldName"
   ```

2. **Index out of bounds**: When attempting to access an offset that doesn't exist
   ```scala
   // Throws: "Index out of bounds at path fieldName, offset N"
   ```

## Implementation Notes

### Multiple Maps Support

The `projectMaps` method accepts a sequence of maps and searches them in order, using the first map that contains the required prefix. This is useful when combining multiple sources of parameters (e.g., query params + form data):

```scala
val queryParams = Map("id" -> IndexedSeq("123"))
val formData = Map("name" -> IndexedSeq("John"))

val result = summon[MapExtract[MyClass]].projectMaps(Seq(queryParams, formData))
```

### Type Safety

All parsing errors (e.g., invalid number formats, invalid UUID strings) will throw standard conversion exceptions from the respective types (`NumberFormatException`, `IllegalArgumentException`, etc.). Consider wrapping extraction calls in error handling appropriate for your use case.

## Integration with lambda-serve

In the lambda-serve framework, the `requestmapped` module integrates MapExtract to automatically extract parameters from HTTP requests:

```scala
import net.lambdaserve.requestmapped.*

case class UserQuery(id: String, includeDetails: Boolean) derives MapExtract

// In your handler
def getUser(query: UserQuery): Response =
  // query is automatically extracted from request parameters
  ???
```

The request data is mapped into a case class by mapping in this order: 
1. Path parameters
2. Query parameters
3. Form data
4. Headers

