# Objetivo del proyecto

NetLikes se trata de una red social cuya piedra angular reside en los gustos de los usuarios, caracterizada por la libertad y control a los usuarios de lo que quieren ver, de lo que quieren hablar y lo que quieran hacer, cuidando siempre una interactividad sana, en la que malos comportamientos y spoilers no son bienvenidos.

## Sprint Zero

Durante el desarrollo de este se asentaron las bases sobre las que tendrá lugar el desarrollo de la aplicación. En primer lugar, se desarrolló una interfaz gráfica sencilla e intiutiva sobre la que el usuario podrá interactuar de forma libre. Asimismo, se diseñó el modelo entidad-relación de la base de datos y se implementó para poder realizar carga de películas a través del catálogo de la API.

## Sprint 1

Durante este sprint se han incorporado funcionalidades esenciales que amplían de manera significativa las capacidades sociales de la aplicación. Se ha integrado Discourse como sistema de foros, permitiendo la creación de espacios de discusión asociados a cada película y fomentando la participación activa de los usuarios. Además, se ha implementado el sistema de creación e interacción entre usuarios, incorporando acciones como añadir películas a sus respectivas colecciones, seguir a otros perfiles, gestionar solicitudes de seguimiento, controlar la privacidad de la cuenta y administrar bloqueos, con el objetivo de garantizar una experiencia segura y personalizada. Finalmente, se ha llevado a cabo el despliegue del proyecto en Google Cloud, convirtiendo la aplicación en un servicio plenamente funcional y accesible públicamente para cualquier usuario interesado en explorar la plataforma.

## Sprint 2

Durante el periodo de trabajo de este se han pulido detalles y acciones que terminan de moldear NetLikes como una red social. Se incluyeron acciones como recomendaciones a usuarios a los que sigues o para añadir en tu perfil, por si quieres convertirlo en un tablón de recomendaciones para aquellos que te sigan. Asimismo, también se añadió un sistema de recomendaciones basado en las interacciones de los usuarios, que permite crear una experiencia personalizada de la red social. Se incluyó también un sistema de chats privados entre usuarios, ya que consideramos que aquellos usuarios que sean más tímidos también deben tener la oportunidad de interactuar con otros usuarios para hablar sobre sus gustos. Además, siguiendo la filosofía de NetLikes, basada en la confianza en nuestros usuarios, se ha habilitado un sistema de denuncia de comentarios para la regulación de los comentarios en los foros por parte de la comunidad. Por último, para poner la guinda a nuestro trabajo, se diseñó un modo de edición en el perfil para que todos los usuarios pudieran decidir con qué se sienten más cómodos para mostrar o qué les representa mejor, pudiendo ocultar listas de su perfil, editar la bio y cambiar su foto de perfil por uno de nuestros avatares predefinidos.

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

# Ejecución de test

Para la ejecución de test, se encuentra una división clara, por una parte, tenenemos los test de backend, y, por el otro, los de frontend.
## Test de frontend
Para ejecutarlos es necesario ejecutar el siguiente comando en el directorio ./NETLIKES/frontend:
```
ng test
```

## Test de backend
Al tener estos mayor complejidad y dependencia con la base de datos hay dos comandos que permiten ejecutar los test, para los test unitarios, en el directorio ./NETLIKES/backend/netlikes, se ejecuta:
```
mvn test
```
Y para ejecutar tanto los test de integración como los test unitarios, en primer lugar, se deben ejecutar los siguientes comandos para crear el contenedor que los soportará:
```
docker run --name netlikes-test-db -e POSTGRES_DB=netlikes_test -e POSTGRES_USER=test -e POSTGRES_PASSWORD=test -p 5433:5432 -d pgvector/pgvector:pg16
docker exec -it netlikes-test-db psql -U test -d netlikes_test -c "CREATE EXTENSION IF NOT EXISTS vector;"
```
Y luego, una vez nos encontremos en el directorio ./NETLIKES/backend/netlikes, se puede ejecutar el comando:
```
mvn verify
```