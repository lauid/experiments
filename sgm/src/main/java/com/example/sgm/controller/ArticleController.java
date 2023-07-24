package com.example.sgm.controller;

import com.example.sgm.common.Result;
import com.example.sgm.common.ResultGenerator;
import com.example.sgm.entity.Article;
import com.example.sgm.mapper.ArticleMapper;
import com.example.sgm.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    ArticleMapper articleMapper;

    @PostMapping("/add")
    public Result<Article> addArticle(@RequestBody Article article) {

        System.out.println(article.toString());
        Integer result = articleService.addArticle(article);

        if (result >= 0) {
            return ResultGenerator.genSuccessResult(result);
        }
        return ResultGenerator.genFailResult("");
    }


    @GetMapping("/get")
    public Result<Article> getArticle(@RequestParam("id") Long id) {

        Long start = System.currentTimeMillis();
        Article article = articleService.getArticle(id);
        Long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));

        System.out.println(article.toString());
        if (null != article)
            return ResultGenerator.genSuccessResult(article);
        return ResultGenerator.genFailResult("");
    }


    /**
     * 更新一篇文章
     *
     * @param contetnt
     * @param id
     * @return
     */
    @GetMapping("/resh")
    public Result<Article> update(@RequestParam("content") String content, @RequestParam("id") Long id) {
        final Integer result = articleService.updateContentById(content, id);
        if (result > 0) {
            return ResultGenerator.genSuccessResult(result);
        } else {
            return ResultGenerator.genFailResult("");
        }
    }

    /**
     * 删除一篇文章
     *
     * @param id
     * @return
     */
    @GetMapping("/rem")
    public Result<Article> remove(@RequestParam("id") Integer id) {
        final Integer result = articleService.removeArticleById(id);
        if (result > 0) {
            return ResultGenerator.genSuccessResult(result);
        } else {
            return ResultGenerator.genFailResult("");
        }
    }

}

