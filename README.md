# Objetivo del proyecto

NetLikes se trata de una red social cuya piedra angular reside en los gustos de los usuarios, caracterizada por la libertad y control a los usuarios de lo que quieren ver, de lo que quieren hablar y lo que quieran hacer, cuidando siempre una interactividad sana, en la que malos comportamientos y spoilers no son bienvenidos.

## Sprint Zero

Durante el desarrollo de este se asentaron las bases sobre las que tendrá lugar el desarrollo de la aplicación. En primer lugar, se desarrolló una interfaz gráfica sencilla e intiutiva sobre la que el usuario podrá interactuar de forma libre. Asimismo, se diseñó el modelo entidad-relación de la base de datos y se implementó para poder realizar carga de películas a través del catálogo de la API.

## Sprint 1

Durante este sprint se han incorporado funcionalidades esenciales que amplían de manera significativa las capacidades sociales de la aplicación. Se ha integrado Discourse como sistema de foros, permitiendo la creación de espacios de discusión asociados a cada película y fomentando la participación activa de los usuarios. Además, se ha implementado el sistema de creación e interacción entre usuarios, incorporando acciones como añadir películas a sus respectivas colecciones, seguir a otros perfiles, gestionar solicitudes de seguimiento, controlar la privacidad de la cuenta y administrar bloqueos, con el objetivo de garantizar una experiencia segura y personalizada. Finalmente, se ha llevado a cabo el despliegue del proyecto en Google Cloud, convirtiendo la aplicación en un servicio plenamente funcional y accesible públicamente para cualquier usuario interesado en explorar la plataforma.

# Ejecución del sitio web

Como podrá observar, nuestro poyecto posee un fichero llamado docker-compose, en el se encuentra lo necesario para construir el contenedor y que el servicio comience a funcionar de forma local.

En primer lugar, deberá añadir a dicho fichero las claves que se encuentra en el fichero "Claves docker-compose.yml", en el directorio NetLikes, de la entrega realizada en el campus virtual, ya que al tratarse de claves asociadas a nuestra imagen no queremos que caiga en las manos equivocadas. En concreto, deberá añadirlas en el apartado enviroment de backend. Una vez realizados estos cambios, y después de haberlos guardado, deberá ejecutar el siguiente comando, asegurándose de que se encuentra en la carpeta raíz del proyecto:

```
docker-compose up --build
```

Una vez haya ejecutado el comando y finalice la carga de películas, podrá buscar la siguiente url en cualquiera de sus buscadores y crearse una cuenta en nuestro sitio web para comenzar a disfrutar de la experiencia NetLikes.

```
http://localhost:4200
```

O si lo prefiere, puede acceder directamente al sitio web sin necesidad de ejecutar nada en local mediante la URL disponible en el apartado ***About***, situado en el lateral derecho del repositorio, o en el fichero “URL NetLikes” incluido en la entrega del Sprint 1 del campus virtual.