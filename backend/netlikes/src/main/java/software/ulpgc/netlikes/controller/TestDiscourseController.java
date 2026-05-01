// package software.ulpgc.netlikes.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import software.ulpgc.netlikes.service.DiscourseService;

// @RestController
// public class TestDiscourseController {

//     @Autowired
//     private DiscourseService discourseService;

//     // Este endpoint escuchará en la ruta /test-forum
//     @GetMapping("/test-forum")
//     public String testCreateForum(@RequestParam String title) {
//         // Llamamos al servicio que acabas de crear
//         Integer topicId = discourseService.createMovieForum(title);
        
//         if (topicId != null) {
//             return "¡Éxito absoluto! Se ha creado el foro para la película '" + title + "'. El ID en Discourse es: " + topicId;
//         } else {
//             return "Vaya, ha fallado la creación. Revisa los logs de tu terminal (Docker) para ver el error de Spring Boot.";
//         }
//     }
// }