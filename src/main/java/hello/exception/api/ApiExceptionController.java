package hello.exception.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiExceptionController {

    @GetMapping("/api/members/{id}")
    public MemberDto getMember(@PathVariable("id")String id){
        if(id.equals("ex")){
            throw new RuntimeException("wrong user");
        }
        if(id.equals("bad")){
            throw new IllegalArgumentException("wrong url");
        }
        return new MemberDto(id, "test-member-"+id);
    }


    @Data
    @AllArgsConstructor
    public static class MemberDto{
        private String memberId;
        private String name;
    }
}
