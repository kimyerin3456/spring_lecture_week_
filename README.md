# artineer_spring_study_week2

-API구조 구현해보기

## Spring Web 진입점 생성

-클라이언트 요청을 받는 Controller객체 생성
-pingpong api구현

## API 구현

* controller/PingController.java

    @RestController
    public class PingController {
      @GetMapping("/")
      public String ping() {
        return "pong";
      }
    }

* dto/Response.java
```
public class Response {
  private String code;
  private String desc;
  private String data;

  public Response(String code, String desc, String data) {
    this.code = code;
    this.desc = desc;
    this.data = data;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
```
```
* controller/PingController.java
@RestController
public class PingController {
    @GetMapping
    public Object ping() {
        return new Response("0000", "정상입니다", "pong");
    }
}
```
-API 응답구조를 만드는 코드가 중복되므로 개선이 필요하다.

## Builder패턴
- 새로운 객체가 필요한 곳에서 직접 생성하기 전에 필수 인자 값들을 전달하여 빌더 객체를 만든 후 빌더 객체에 정의된 메서드를 호출해서 객체를 생성하는 것
- 점층적 생성자 패턴(생성자 오버로딩을 통한 가독성 높임)과 자바 빈 패턴의 장점(SETTER메서드를 통해 퍼블릭 메서드를 많이 만들어서 란번의 생성자 호출로 생성을 끝낼 수 없다.
)을 결합
- lombok을 활용해서 보일러플레이트 코드를 없애고 더 편하게 구현 할 수 있다.

## lombok 의존성 추가
```
dependencies {
	compileOnly 'org.projectlombok:lombok:1.18.20'
	annotationProcessor 'org.projectlombok:lombok:1.18.20'
}
```
## 동적 타입의 활용
-항상 스트링 타입을 받는 것을 제네릭 문법을 통해 개선할 수 있다.

```
@Getter
@Builder
public class Response<T> {
    private final ApiCode code;
    private final T data;
}
```
```
    @RestController
public class PingController {
    @GetMapping
    public Response<String> ping() {
        return Response.<String>builder()
                .code(ApiCode.SUCCESS)
                .data("pong")
                .build();
    }
}
```
## ResponseEntity 객체 활용해보기
-Http Status Code 직접 관리해야 할 때 사용
```
@RestController
public class PingController {
  @GetMapping
  public ResponseEntity<Response<String>> ping() {
    Response<String> response = Response.<String>builder()
            .code(ApiCode.SUCCESS).data("pong").build();

    return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
  }
} 
```
## REST API
* HTTP 메서드로 행위 표현
	* @GetMapping:조회
	* @PostMapping:생성
	* @PutMapping: 변경
	* @DeleteMapping: 삭제
* REST 구조 장점
	* 클라이언트를 위한 API가 아닌 비지니스 도메인을 표현하는 API이다.
	* Resource/domain을 중심으로 표현
	
## Article 객체생성
```
@Getter
@Builder
public class Article {
  Long id;
  String title;
  String content;
}
```

## Article Service
```
@Service
public class ArticleService {
    private Long id=0L;
    final List<Article> database = new ArrayList<>();


    public Long save(Article request) {
        Article domain = Article.builder()
                .id(getId())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        database.add(domain);
        return domain.getId();
    }
    private Long getId() {
        return ++id;
    }
    public Article findById(long id){
        return database.stream().filter(article -> article.getId().equals(id)).findFirst().get();
    }


}
```

## Article Controller(서비스 생성자 주입,post 구현, get 구현)
```
@RequiredArgsConstructor
@RequestMapping("/api/v1/article")
@RestController
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping
    public Response<Long> post(@RequestBody ArticleDto.ReqPost request) {
        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        Long id = articleService.save(article);

        return Response.<Long>builder()
                .code(ApiCode.SUCCESS)
                .data(id)
                .build();
    }

    @GetMapping("/{id}")
    public Response<ArticleDto.Res> get(@PathVariable Long id){
        Article article = articleService.findById(id);

        ArticleDto.Res response= ArticleDto.Res.builder()
                .id(String.valueOf(article.getId()))
                .title(article.getTitle())
                .content(article.getContent())
                .build();

        return Response.<ArticleDto.Res>builder()
                .code(ApiCode.SUCCESS)
                .data(response)
                .build();
    }
}
```

## ArticleDto
* http통신을 할 때만 사용되는 객체로 도메인 레이어와 프레젠테이션 레이어가 구분이 되어야 함.
```
public class ArticleDto {
    @Getter
    public static class ReqPost{
        String title;
        String content;
    }
    @Builder
    public static class Res{
        private String id;
        private String title;
        private String content;
    }
}
```

