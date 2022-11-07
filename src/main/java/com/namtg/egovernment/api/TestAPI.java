//package com.namtg.egovernment.api;
//
//import com.namtg.egovernment.dto.response.ResponseCase;
//import com.namtg.egovernment.dto.response.ServerResponseDto;
//import com.namtg.egovernment.entity.test.StudentEntity;
//import com.namtg.egovernment.job.DeleteCommentJob;
//import com.namtg.egovernment.job.NotifyConclusionPostJob;
//import com.namtg.egovernment.util.export.FactoryExport;
//import com.namtg.egovernment.enum_common.TypeExport;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletResponse;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Function;
//
//@RestController
//@RequestMapping("/api/test")
//public class TestAPI {
//    private static final String SCHEDULED_TASKS = "scheduledTasks";
//
//    @Autowired
//    private ScheduledAnnotationBeanPostProcessor postProcessor;
//
//    @Autowired
//    private DeleteCommentJob deleteCommentJob;
//
//    @Autowired
//    private NotifyConclusionPostJob notifyConclusionPostJob;
//
//    @PostMapping("/notifyConclusionPost")
//    public void notifyConclusionPost() {
//        notifyConclusionPostJob.notifyConclusionPost();
//    }
//
//    @PostMapping("/sendMail")
//    public ResponseEntity<ServerResponseDto> sendMail() {
//        return ResponseEntity.ok(new ServerResponseDto(ResponseCase.SUCCESS));
//    }
//
//    @GetMapping(value = "/start")
//    public String startSchedule() {
//        System.out.println("Start cron");
//        postProcessor.postProcessAfterInitialization(deleteCommentJob, SCHEDULED_TASKS);
//        return "OK";
//    }
//
//    @GetMapping(value = "/stop")
//    public String stopSchedule() {
//        System.out.println("Stop cron");
//        postProcessor.postProcessBeforeDestruction(deleteCommentJob, SCHEDULED_TASKS);
//        return "OK";
//    }
//
//    @GetMapping("/exportCsv")
//    public void exportCsv(HttpServletResponse response) {
//        List<StudentEntity> listStudent = new ArrayList<>();
//        listStudent.add(new StudentEntity("Nam", 22));
//        listStudent.add(new StudentEntity("Hoa", 21));
//
//        String[] lineHeader = new String[]{"Name", "Age"};
//        Function<StudentEntity, String[]> convertFunction = this::convertEntityToStringArray;
//        String fileName = "test";
//
//        FactoryExport
//                .exportFileWithType(TypeExport.EXCEL)
//                .exportFile(response, lineHeader, listStudent, convertFunction, fileName, null, null);
//    }
//
//    private String[] convertEntityToStringArray(StudentEntity entity) {
//        String[] data = new String[2];
//        data[0] = entity.getName();
//        data[1] = String.valueOf(entity.getAge());
//        return data;
//    }
//}
