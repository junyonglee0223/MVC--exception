package hello.exception.api;

import hello.exception.exception.BadRequestException;
import hello.exception.exception.UserException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ApiExceptionController {

    @GetMapping("/api/default-handler-ex")
    public String defaultException(@PathVariable Integer data){
        return "ok";
    }

    @GetMapping("/api/response-status-ex1")
    public String responseStatusEx1(){
        throw new BadRequestException();
    }
    @GetMapping("/api/response-status-ex2")
    public String responseStatusEx2(){
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "error.bad",
                new IllegalArgumentException());
    }

    @GetMapping("/api/members/{id}")
    public MemberDto getMember(@PathVariable("id")String id){
        if(id.equals("ex")){
            throw new RuntimeException("wrong user");
        }
        if(id.equals("bad")){
            throw new IllegalArgumentException("wrong url");
        }
        if(id.equals("user-ex")){
            throw new UserException("user error occurs!!");
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
