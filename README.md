#  HTTP and the modern Web 
## Refactoring & MultiThreading
### Description
It is necessary to refactor the <a href="https://github.com/netology-code/jspr-code.git">code discussed at the lecture</a> and apply all the knowledge that you have:

1. Allocate a Server class with methods for:
* startup;
* processing a specific connection.
2. Implement connection processing using ThreadPool — allocate a fixed 64 threads, and process each connection in a thread from the pool.

<a href="https://github.com/netology-code/jspr-homeworks/tree/master/01_web">(RUS version of description)</a>

## Handlers* 
### Description
The server that you wrote in the previous task (Refactoring & MultiThreading) is not yet extensible.
Try to make it more useful — so that you can add handlers to certain path templates to the server.
This means that you need to be able to do so:

```java
public class Main {
    public static void main(String[] args){
      final var server = new Server();  
      // код инициализации сервера (из вашего предыдущего ДЗ)

      // добавление хендлеров (обработчиков)    
      server.addHandler("GET", "/messages", new Handler() {
        public void handle(Request request, BufferedOutputStream responseStream) {
          // TODO: handlers code
        }
      });
      server.addHandler("POST", "/messages", new Handler() {
        public void handle(Request request, BufferedOutputStream responseStream) {
          // TODO: handlers code
        }
      });

      server.listen(9999);
    }    
}
```

As a result, the first handler will be called for a GET request to the path "/messages", and the second handler will be called for a POST request and the path "/messages".

As you can see, ```Handler``` is a functional interface with just one method. It can be replaced with ```lambda```.

Request is a class that you design yourself. It is important for us that it contains:
* request method, because the same handler can be assigned to different methods;
* request headers;
* request body, if any.
* ```BufferedOutputStream``` is taken by wrapping the ```OutputStream``` socket: ```new BufferedOutputStream(socket.getOutputStream())```.

### Task
Implement the requirements specified in the legend.
1. Hints on implementation.
2. You accept the request, parse it in its entirety, as we did in the lecture, and collect an object of the ```Request``` type.
3. Based on the data from the ```Request```, you choose a handler (there can only be one), which will process the request.
4. All handlers should be stored in ```Server``` fields.
5. The easiest way to store handlers is to use method and path as keys. You can make both a ```Map``` inside a ```Map```, and separate ```Map``` for each method.
6. The handler search consists in the fact that you select all registered handlers by the desired method, and then iterate along the way. Use the exact match for now: consider that you have all queries without a Query String.
7. Having found the right handler, it is enough to call its ```handle``` method, passing ```Request``` and ```BufferedOutputStream``` there.
8. Since your server is multithreaded, think about how you will safely store all the handlers.
9. As a Body, it is enough to pass ```InputStream``` (we remind you, Body starts after ```\r\n\r\n```).

Total: in fact, you solve the problem of searching for an element in the "collection" by calling its method.

<a href="https://github.com/netology-code/jspr-homeworks/tree/master/01_web">(RUS version of description)</a>
