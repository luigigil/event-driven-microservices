# Event Driven Microservices

<!---Esses são exemplos. Veja https://shields.io para outras pessoas ou para personalizar este conjunto de escudos. Você pode querer incluir dependências, status do projeto e informações de licença aqui--->

![GitHub repo size](https://img.shields.io/github/repo-size/luigigil/event-driven-microservices?style=for-the-badge)
![GitHub language count](https://img.shields.io/github/languages/count/luigigil/event-driven-microservices?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/luigigil/event-driven-microservices?style=for-the-badge)
![Bitbucket open issues](https://img.shields.io/bitbucket/issues/luigigil/event-driven-microservices?style=for-the-badge)
![Bitbucket open pull requests](https://img.shields.io/bitbucket/pr-raw/luigigil/event-driven-microservices?style=for-the-badge)
![](https://img.shields.io/github/languages/top/luigigil/event-driven-microservices?style=for-the-badge)

<!-- <img src="exemplo-image.png" alt="exemplo imagem"> -->

##  Introduction

There are many patterns that can be found for building production-ready software. A good start point could be looking at Chris Richardson's [site](https://microservices.io/patterns/index.html).

This repository was initially built as part of the author's study from Sergey Kargopolov's [course](https://www.udemy.com/course/spring-boot-microservices-cqrs-saga-axon-framework/). The main objective was a build a simple event-driven [microservices](https://microservices.io/patterns/microservices.html) system, as well as the application of patterns like [CQRS](https://microservices.io/patterns/data/cqrs.html), [Event-Sourcing](https://microservices.io/patterns/data/event-sourcing.html), [Saga](https://microservices.io/patterns/data/saga.html), [Database per Service](https://microservices.io/patterns/data/database-per-service.html) and [Domain event](https://microservices.io/patterns/data/domain-event.html).

Next challenges include extending functionalities of this project, adding a nice frontend interface, and explore the patterns above more in depth, as well as impleting other interesting patterns to solve problems that might happen.

##  To do

This project is still under development. The next updates will add:

- [x] Initial Readme
- [ ] Readme for each module
- [ ] Wiki
- [ ] Frontend
- [ ] Backlog of new features

## 💻 Prerequisites

Before starting, we'll need this:
<!---Estes são apenas requisitos de exemplo. Adicionar, duplicar ou remover conforme necessário--->
* Java 11
* Docker and Docker-Compose

## 🚀 Getting started

In order to start up the project, you'll need an instance of [Axon Server](https://axoniq.io/product-overview/axon-server)

Axon Server: run this command inside `axon-server` folder.
```
sh ./run-axon.sh
```
In case that is any doubt on running axon server in a docker container, there's an axon's blog post that can be helpful: https://axoniq.io/blog-overview/running-axon-server-in-docker


Start running first `discovery-server`. Then run `orders-service`, `payments-service`, `products-service` and `users-service`. The last one will be `api-gateway`.


## ☕ Working with the API

### products-service

#### Creating a product

`POST /products-service/products`

     curl -i -H 'Accept: application/json' \
      -H 'Content-Type: application/json' \
      -d '{ "title": "Foo", "price": 10, "quantity": 2 }' \
      http://localhost:8082/products-service/products

### Response

    HTTP/1.1 200 OK
    Content-Type: application/json
    Date: Thu, 02 Dec 2021 15:53:56 GMT
    content-length: 36

    11dee306-0103-4c33-97f9-76b43112f096

### orders-service

#### Placing an order for a product

`POST /orders-service/orders`

     curl -i -H 'Accept: application/json' \
      -H 'Content-Type: application/json' \
      -d '{
            "productId": "11dee306-0103-4c33-97f9-76b43112f096",
            "quantity": 1,
            "addressId": "afbb5881-a872-4d13-993c-faeb8350eea5"
        }' \
      http://192.168.0.3:8082/orders-service/orders

### Response

    HTTP/1.1 200 OK
    transfer-encoding: chunked
    Content-Type: application/json
    Date: Thu, 02 Dec 2021 15:58:38 GMT

    {"orderId":"b9a1e7a3-b147-4f88-86c1-edc08f653912","orderStatus":"APPROVED","message":""}

For viewing Axon Dashboard: http://localhost:8024/

For viewing discovery-service interface: http://localhost:8761/

[⬆ Back to top](#event-driven-microservices)<br>