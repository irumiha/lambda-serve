# Routing in lambda-serve

## Overview

The routing mechanism in lambda-serve provides a type-safe, regex-based approach to defining HTTP routes. Routes are defined using the `Router` and `Route` types, with support for path parameters, HTTP methods, and composable router hierarchies.

## Core Concepts

### Route Handler

A route handler is a simple function that takes a `Request` and returns a `Response`:

```scala
type RouteHandler = Request => Response
```

### Route Definition

A `Route` consists of three parts:
- **HTTP Method**: The HTTP method (GET, POST, PUT, DELETE, etc.)
- **Path Pattern**: A Scala `Regex` that matches the request path
- **Handler**: A `RouteHandler` function that processes the request

```scala
case class Route(method: Method, path: Regex, handler: RouteHandler)
```

### Router

A `Router` contains a sequence of routes and provides methods to match incoming requests against defined routes:

```scala
case class Router(routes: Seq[Route])
```

## Creating Routes

### Basic Route Definition

Use `Router.make` to create a router with routes:

```scala
import net.lambdaserve.*
import net.lambdaserve.http.Method.*

val router = Router.make(
  GET -> "/hello".r -> { request =>
    Response.Ok("Hello, World!")
  },
  POST -> "/submit".r -> { request =>
    Response.Ok("Data submitted")
  }
)
```

The syntax follows the pattern:
```scala
Method -> PathRegex -> HandlerFunction
```

### Using Methods as Handlers

Instead of lambda functions, you can reference methods directly:

```scala
class UserController:
  def getUser(request: Request): Response =
    Response.Ok("User data")

  def createUser(request: Request): Response =
    Response.Ok("User created")

  val router = Router.make(
    GET  -> "/user".r -> getUser,
    POST -> "/user".r -> createUser
  )
```

This approach is useful for organizing complex logic and improving code reusability.

### Inline Lambda Handlers

For simple handlers, inline lambdas work well:

```scala
val router = Router.make(
  GET -> "/status".r -> { request =>
    Response.Ok("Server is running")
  },
  GET -> "/time".r -> { request =>
    Response.Ok(java.time.LocalDateTime.now().toString)
  }
)
```

## Path Patterns

### Exact Matching

Use simple regex patterns for exact path matching:

```scala
Router.make(
  GET -> "/".r -> homeHandler,
  GET -> "/about".r -> aboutHandler,
  GET -> "/contact".r -> contactHandler
)
```

Note: The trailing `/` must be explicitly matched if required:
```scala
GET -> "/path/?".r  // Matches both /path and /path/
```

### Path Parameters

Path parameters are defined using **named capture groups** in regex:

```scala
Router.make(
  // Match /user/123 and capture "123" as userId
  GET -> "/user/(?<userId>\\d+)".r -> { request =>
    val userId = request.pathParams("userId").head
    Response.Ok(s"User ID: $userId")
  },

  // Match /posts/my-post-title
  GET -> "/posts/(?<slug>[\\w-]+)".r -> { request =>
    val slug = request.pathParams("slug").head
    Response.Ok(s"Post: $slug")
  }
)
```

Path parameters are automatically extracted and added to `request.pathParams` as `Map[String, IndexedSeq[String]]`.

#### Named Capture Groups

The syntax for named capture groups is: `(?<name>pattern)`

```scala
// Single parameter
"/product/(?<id>\\d+)".r

// Multiple parameters
"/(?<category>\\w+)/(?<subcategory>\\w+)/(?<item>\\d+)".r

// Optional trailing slash
"/article/(?<slug>[\\w-]+)/?".r
```

### Wildcard and Flexible Patterns

```scala
Router.make(
  // Match any path segment
  GET -> "/api/(?<resource>\\w+)".r -> dynamicHandler,

  // Match multiple path segments
  GET -> "/files/(?<path>.+)".r -> fileHandler,

  // Match with optional extension
  GET -> "/document/(?<name>\\w+)(?<ext>\\.\\w+)?".r -> documentHandler
)
```

### Common Regex Patterns

| Pattern | Description | Example Matches |
|---------|-------------|----------------|
| `\\d+` | One or more digits | `123`, `42` |
| `\\w+` | Word characters (letters, digits, underscore) | `user`, `post_123` |
| `[a-zA-Z]+` | Letters only | `hello`, `World` |
| `[\\w-]+` | Words and hyphens (slugs) | `my-post`, `hello_world` |
| `.+` | Any characters (greedy) | `path/to/file.txt` |
| `.*?` | Any characters (non-greedy) | `foo` in `foo/bar` |
| `/?` | Optional trailing slash | Matches both `/path` and `/path/` |

## Combining Routers

### Router Composition

Use `Router.combine` to mount multiple routers under different path prefixes:

```scala
val apiRouter = Router.make(
  GET -> "/users".r -> listUsers,
  GET -> "/posts".r -> listPosts
)

val adminRouter = Router.make(
  GET -> "/dashboard".r -> adminDashboard,
  GET -> "/settings".r -> adminSettings
)

val mainRouter = Router.combine(
  "/api"   -> apiRouter,    // Routes: /api/users, /api/posts
  "/admin" -> adminRouter   // Routes: /admin/dashboard, /admin/settings
)
```

The prefix is prepended to each route's path pattern.

### Nested Router Composition

Routers can be combined hierarchically:

```scala
val v1Router = Router.make(
  GET -> "/users".r -> usersV1,
  GET -> "/posts".r -> postsV1
)

val v2Router = Router.make(
  GET -> "/users".r -> usersV2,
  GET -> "/posts".r -> postsV2
)

val apiRouter = Router.combine(
  "/v1" -> v1Router,
  "/v2" -> v2Router
)

val mainRouter = Router.combine(
  "/api" -> apiRouter,
  "/"    -> staticRouter
)
// Results in routes like: /api/v1/users, /api/v2/posts, etc.
```

### Controller-Based Organization

A common pattern is to organize routes into controller classes:

```scala
class UserController:
  def getUser(request: Request): Response = ???
  def createUser(request: Request): Response = ???
  def updateUser(request: Request): Response = ???

  val router = Router.make(
    GET  -> "/(?<id>\\d+)".r -> getUser,
    POST -> "/".r -> createUser,
    PUT  -> "/(?<id>\\d+)".r -> updateUser
  )

class ProductController:
  def listProducts(request: Request): Response = ???
  def getProduct(request: Request): Response = ???

  val router = Router.make(
    GET -> "/".r -> listProducts,
    GET -> "/(?<id>\\d+)".r -> getProduct
  )

// Combine all controllers
val appRouter = Router.combine(
  "/api/users"    -> UserController().router,
  "/api/products" -> ProductController().router
)
```

## Accessing Request Data

### Path Parameters

Path parameters are extracted from the URL path using named capture groups:

```scala
GET -> "/user/(?<userId>\\d+)/post/(?<postId>\\d+)".r -> { request =>
  val userId = request.pathParams("userId").head
  val postId = request.pathParams("postId").head
  Response.Ok(s"User $userId, Post $postId")
}
```

### Query Parameters

Query parameters are accessed via `request.query`:

```scala
GET -> "/search".r -> { request =>
  val term = request.query.get("q").flatMap(_.headOption).getOrElse("")
  val page = request.query.get("page").flatMap(_.headOption).getOrElse("1")
  Response.Ok(s"Searching for: $term (page $page)")
}
// Example: /search?q=scala&page=2
```

### Form Data

Form data from POST requests is accessed via `request.form`:

```scala
POST -> "/login".r -> { request =>
  val username = request.form.get("username").flatMap(_.headOption).getOrElse("")
  val password = request.form.get("password").flatMap(_.headOption).getOrElse("")
  Response.Ok(s"Login attempt for user: $username")
}
```

### Combining Parameters

All parameter types can be used together:

```scala
POST -> "/users/(?<userId>\\d+)/update".r -> { request =>
  val userId = request.pathParams("userId").head
  val action = request.query.get("action").flatMap(_.headOption).getOrElse("update")
  val name = request.form.get("name").flatMap(_.headOption).getOrElse("")

  Response.Ok(s"$action user $userId with name: $name")
}
// Example: POST /users/123/update?action=edit
// Body: name=John
```

## HTTP Methods

All standard HTTP methods are supported:

```scala
import net.lambdaserve.http.Method.*

Router.make(
  GET    -> "/resource".r -> getHandler,
  POST   -> "/resource".r -> createHandler,
  PUT    -> "/resource".r -> updateHandler,
  PATCH  -> "/resource".r -> patchHandler,
  DELETE -> "/resource".r -> deleteHandler,
  HEAD   -> "/resource".r -> headHandler,
  OPTIONS -> "/resource".r -> optionsHandler
)
```

## Route Matching

### Match Order

Routes are matched in the order they are defined. The **first matching route** is used:

```scala
Router.make(
  GET -> "/user/admin".r -> adminHandler,    // More specific
  GET -> "/user/(?<id>\\w+)".r -> userHandler // More general
)
// Order matters! /user/admin will match adminHandler, not userHandler
```

If the order were reversed, `/user/admin` would incorrectly match the `userHandler`.

### Finding Routes

The router provides utility methods:

```scala
val router = Router.make(/* routes */)

// Find routes matching a specific path
val routes: Seq[Route] = router.findRoutesForPath("/api/users")

// Print all routes (useful for debugging)
val routeDescriptions: Seq[String] = router.printRoutes
routeDescriptions.foreach(println)
```

## Advanced Patterns

### Type-Safe Request Mapping

Use the `mapped` function with `MapExtract` for automatic parameter extraction:

```scala
import net.lambdaserve.requestmapped.mapped
import net.lambdaserve.mapextract.MapExtract

case class CreateUserCommand(
  name: String,
  email: String,
  age: Int
) derives MapExtract

def createUser(cmd: CreateUserCommand): Response =
  Response.Ok(s"Creating user: ${cmd.name}")

Router.make(
  POST -> "/users".r -> mapped(createUser)
)
```

The `mapped` function automatically extracts and validates parameters from the request. See `query-mapping.md` for details.

### Custom Path Parameter Extraction

For complex scenarios, you can extract path parameters manually:

```scala
GET -> "/(?<year>\\d{4})/(?<month>\\d{2})/(?<day>\\d{2})".r -> { request =>
  val year = request.pathParams("year").head.toInt
  val month = request.pathParams("month").head.toInt
  val day = request.pathParams("day").head.toInt

  val date = java.time.LocalDate.of(year, month, day)
  Response.Ok(s"Date: $date")
}
```

### Static File Serving

Use a catch-all pattern for serving static files:

```scala
GET -> "/static/(?<path>.+)".r -> { request =>
  val path = request.pathParams("path").head
  // Serve file from filesystem
  serveStaticFile(path)
}
```

### API Versioning

Organize routes by API version:

```scala
val v1Router = Router.make(
  GET -> "/users".r -> usersV1Handler
)

val v2Router = Router.make(
  GET -> "/users".r -> usersV2Handler
)

Router.combine(
  "/api/v1" -> v1Router,
  "/api/v2" -> v2Router
)
```

## Best Practices

1. **Order Routes by Specificity**: Define more specific routes before general ones
   ```scala
   GET -> "/users/me".r -> currentUserHandler,
   GET -> "/users/(?<id>\\d+)".r -> userByIdHandler
   ```

2. **Use Controllers for Organization**: Group related routes into controller classes
   ```scala
   class UserController:
     val router = Router.make(/* user routes */)
   ```

3. **Consistent Path Patterns**: Use consistent naming and patterns across your API
   ```scala
   // Good: consistent plural resource names
   GET -> "/users/(?<id>\\d+)".r
   GET -> "/posts/(?<id>\\d+)".r
   ```

4. **Handle Trailing Slashes**: Explicitly handle optional trailing slashes
   ```scala
   GET -> "/path/?".r  // Matches both /path and /path/
   ```

5. **Validate Path Parameters**: Extract and validate parameters with proper error handling
   ```scala
   GET -> "/user/(?<id>\\d+)".r -> { request =>
     val userId = request.pathParams.get("id")
       .flatMap(_.headOption)
       .flatMap(_.toIntOption)
       .fold(Response.BadRequest("Invalid user ID")){ userId =>
          ??? // continue with valid userId 
       }
   }
   ```

6. **Use Type-Safe Mapping**: Prefer `mapped` with `MapExtract` for complex parameter extraction
   ```scala
   POST -> "/users".r -> mapped(createUser)
   ```

7. **Document Complex Patterns**: Add comments for non-obvious regex patterns
   ```scala
   // Match ISO date format: YYYY-MM-DD
   GET -> "/date/(?<date>\\d{4}-\\d{2}-\\d{2})".r -> dateHandler
   ```

## Error Handling

Routes should handle errors gracefully:

```scala
GET -> "/user/(?<id>\\d+)".r -> { request =>
  try {
    val userId = request.pathParams("id").head.toInt
    val user = findUser(userId)
    Response.Ok(user)
  } catch {
    case _: NumberFormatException =>
      Response.BadRequest("Invalid user ID format")
    case _: NoSuchElementException =>
      Response.NotFound("User not found")
    case e: Exception =>
      Response.InternalServerError(s"Error: ${e.getMessage}")
  }
}
```

## Integration Example

Complete example showing all concepts:

```scala
import net.lambdaserve.*
import net.lambdaserve.http.Method.*
import net.lambdaserve.server.jetty.JettyServer

class UserController:
  def listUsers(request: Request): Response =
    val page = request.query.get("page").flatMap(_.headOption).getOrElse("1")
    Response.Ok(s"Users page $page")

  def getUser(request: Request): Response =
    val userId = request.pathParams("id").head
    Response.Ok(s"User $userId")

  val router = Router.make(
    GET -> "/".r -> listUsers,
    GET -> "/(?<id>\\d+)".r -> getUser
  )

class PostController:
  def listPosts(request: Request): Response = Response.Ok("Posts")
  def getPost(request: Request): Response =
    val postId = request.pathParams("id").head
    Response.Ok(s"Post $postId")

  val router = Router.make(
    GET -> "/".r -> listPosts,
    GET -> "/(?<id>\\d+)".r -> getPost
  )

@main def server(): Unit =
  val appRouter = Router.combine(
    "/api/users" -> UserController().router,
    "/api/posts" -> PostController().router,
    "/" -> Router.make(
      GET -> "/".r -> { _ => Response.Ok("Welcome") },
      GET -> "/health".r -> { _ => Response.Ok("OK") }
    )
  )

  JettyServer
    .makeServer(
      host = "localhost",
      port = 8080,
      router = appRouter
    )
    .join()
```
