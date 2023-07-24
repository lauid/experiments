package com.example.sgm.service;

import com.example.sgm.entity.Article;
import com.example.sgm.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@CacheConfig(cacheNames = "articleCache", cacheManager = "cacheManager")
public class ArticleService {

    private AtomicInteger count = new AtomicInteger(0);

    @Autowired
    private ArticleMapper articleMapper;


    /**
     * 增加一篇文章 每次就进行缓存
     *
     * @return
     */
    @CachePut(key = "#article.id")
    public Integer addArticle(Article article) {
        int result = articleMapper.insert(article);
        if (result > 0) {
            Long lastInertId = article.getId();
            System.out.println("--执行增加操作--id:" + lastInertId);
        }
        return result;
    }

    /**
     * 获取文章  以传入的id为键，当state为0的时候不进行缓存
     *
     * @param id 文章id
     * @return
     */
    @Cacheable(key = "#id", unless = "#result.state==0")
    public Article getArticle(Long id) {
        try {
            //模拟耗时操作
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final Article article = articleMapper.selectById(id);
        System.out.println("--执行数据库查询操作" + count.incrementAndGet() + "次" + "id:" + id);
        return article;
    }

    /**
     * 通过id更新内容 清除以id作为键的缓存
     *
     * @param id
     * @return
     */
    @CacheEvict(key = "#id")
    public Integer updateContentById(String content, Long id) {
        Article article = new Article();
        article.setId(id);
        article.setContent(content);
        Integer result = articleMapper.updateById(article);
        System.out.println("--执行更新操作id:--" + id);
        return result;
    }

    /**
     * 通过id移除文章
     *
     * @param id 清除以id作为键的缓存
     * @return
     */
    @CacheEvict(key = "#id")
    public Integer removeArticleById(Integer id) {
        final Integer result = articleMapper.deleteById(id);
        System.out.println("执行删除操作,id:" + id);
        return result;
    }

}
