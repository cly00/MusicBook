package com.cn.controller;

import com.cn.*;
import com.cn.config.JwtTokenUtil;
import com.cn.pojo.LogInDTO;
import com.cn.pojo.SignUpDTO;
import com.cn.entity.Carousel;
import com.cn.entity.CarouselCategory;
import com.cn.entity.Essay;
import com.cn.entity.User;
import com.cn.util.RestUtil;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 描述:
 * 测试
 *
 * @author ngcly
 * @create 2018-08-05 14:48
 */
@Api(value = "IndexController", tags = "首页内容相关API")
@RestController
@RequiredArgsConstructor
public class IndexController {
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final EssayService essayService;
    private final NoticeService noticeService;
    private final CarouselService carouselService;
    private final ClassifyService classifyService;
    private final BookService bookService;

    @ApiOperation(value = "登录", notes = "用户登录")
    @PostMapping("/signin")
    public ModelMap postAccessToken(@RequestBody LogInDTO logInDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(logInDTO.getUsername(),logInDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(user);
        return RestUtil.success(token);
    }

    @ApiOperation(value = "刷新token", notes = "用户刷新token")
    @GetMapping("/signin")
    public ModelMap getAccessToken(HttpServletRequest request) {
        return RestUtil.success(jwtTokenUtil.refreshToken(request.getHeader("authorization")));
    }

    @ApiOperation(value = "登出", notes = "退出登录")
    @DeleteMapping("/signout/{token}")
    public ModelMap logout(@PathVariable("token")String token){
        return RestUtil.success();
    }

    /**
     * 注册
     * @param signUpDTO
     * @return
     */
    @ApiOperation(value = "注册", notes = "用户注册")
    @PostMapping("/signup")
    public ModelMap registerUser(@Valid @RequestBody SignUpDTO signUpDTO, BindingResult result) {
        if(result.hasErrors()){
            return RestUtil.failure(400,result.getFieldError().getField()+":"+result.getFieldError().getDefaultMessage());
        }
        User user = new User();
        BeanUtils.copyProperties(signUpDTO, user);
        return userService.signUp(user);
    }

    @ApiOperation(value = "激活", notes = "用户注册激活")
    @GetMapping("/active/{code}")
    public ModelMap activeUser(@PathVariable("code")String code){
        return userService.activeUser(code);
    }

    /**
     * 获取文章列表
     */
    @ApiOperation(value = "文章列表", notes = "获取首页文章简介列表")
    @GetMapping("/essay/{pageSize}/{page}")
    public ModelMap getEssayList(@PathVariable int pageSize,@PathVariable int page){
        return essayService.getEssayList(page,pageSize);
    }

    /**
     * 根据id获取文章详情
     */
    @ApiOperation(value = "文章详情", notes = "根据文章Id获取文章详情")
    @GetMapping("/essay/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "文章ID",paramType = "path",dataType = "string")
    })
    public ModelMap getEssayDetail(@PathVariable String id){
        Essay essay = essayService.getEssayDetail(id);
        return RestUtil.success(essay);
    }

    @ApiOperation(value = "阅读文章", notes = "阅读文章 阅读数+1")
    @PutMapping("/essay/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "文章ID",paramType = "path",dataType = "string")
    })
    public ModelMap readEssay(@PathVariable String id){
        essayService.readEssay(id);
        return RestUtil.success();
    }

    /**
     * 获取文章评论
     */
    @ApiOperation(value = "获取文章评论", notes = "根据文章Id和页数获取评论")
    @GetMapping("/comments/{id}/{page}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "文章ID",paramType = "path",dataType = "string"),
            @ApiImplicitParam(name = "page",value = "页数",paramType = "path",dataType = "int")
    })
    public ModelMap getEssayComment(@PathVariable String id,@PathVariable Integer page){
        return RestUtil.success(essayService.getComments(id,page));
    }

    @ApiOperation(value = "获取分类列表", notes = "获取文章分类列表")
    @GetMapping("/classify")
    public ModelMap getClassifyList(){
        return RestUtil.success(classifyService.getClassifyList());
    }

    /**
     * 获取轮播图
     */
    @ApiOperation(value = "轮播图", notes = "获取轮播图列表")
    @GetMapping("/carousel")
    public ModelMap getCarousel(){
        CarouselCategory carouselCategory = carouselService.getCarouselDetail("index");
        List<Carousel> carousels = null;
        if(carouselCategory!=null){
            carousels = carouselCategory.getCarousels();
        }
        return RestUtil.success(carousels);
    }

    /**
     * 获取公告
     */
    @ApiOperation(value = "公告", notes = "获取展示公告")
    @GetMapping("/notice")
    public ModelMap getNotice(){
        return noticeService.getNotice();
    }

    /**
     * 全文搜索
     */
    @ApiOperation(value = "搜索", notes = "文章搜索")
    @GetMapping("/search/{pageSize}/{page}/{keyword}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword",value = "关键字",paramType = "path",dataType = "string"),
    })
    public ModelMap search(@PathVariable int pageSize,@PathVariable int page,@PathVariable("keyword")String keyword){
        return bookService.highLightSearchEssay(keyword, PageRequest.of(page - 1, pageSize));
    }
}