# URL Shortener

## Description

- This is a URL shortener service that allows users to create,retrieve, update, and delete shortened URLs.
- The service is built with Spring Boot and uses a database to store the URLs and their associated metadata.
## Features

- Create new shortened URLs with optional TTL (Time To Live)
- Retrieve the original URL and its metadata by its ID
- Update the URL associated with an ID
- Delete URLs by their ID

## Endpoints

### GET

- `localhost:8080/api/id`: Get the original URL and its metadata by its ID
- `localhost:8080/api/urls`: Get all URLs and their metadata

### POST

- `localhost:8080/api/`: Create a new shortened URL

`The request body should be a JSON object with the following fields:`
- `"url": "sample.com"`: The original URL to be shortened.

`optional`
- `"id"  : "sampleID"`: The ID for the URL. If not provided, a unique ID will be generated.
- `"ttl": 42069`: The TTL for the URL in int seconds. If not provided, the URL will stay forever.

### DELETE

- `localhost:8080/api/id`: Delete the URL by its ID

### PUT

- `localhost:8080/api/id?url=sample`: Update the URL associated with the ID

### PATCH *

- `id change must be last`
- {
- "url":"xxx21.com",
- "ttl":1200, `<- only changebale if previously not provided, else results in negative ttl`
- "id":23
- }

## Database Structure

The database stores the following information for each URL:

| ID      | URL     | TTL                                   | DATA                       |
|:---------|:---------|:--------------------------------------|:-------------------------|
| `string` | `string` | `Empty for inf or higher for counter` | `local time of creation` |


## Usage

- To create a new shortened URL, make a POST request to `localhost:8080/api` with the request body as described above.

- To get the original URL and its metadata by its ID, make a GET request to `localhost:8080/api/id`.

- To update the URL associated with an ID, make a PUT request to `localhost:8080/api/id?url=sample` as described above.

- To update all URL associated with an ID, make a PATCH request by given schema. *

- To delete a URL by its ID, make a DELETE request to `localhost:8080/api/id`.

## Credits

``Mikita Wagner``

- ``FOR MORE FUN TRY "INTERACTIVE" WEB VERSION``