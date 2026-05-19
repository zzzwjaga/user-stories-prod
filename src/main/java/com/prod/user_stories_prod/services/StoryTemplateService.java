package com.prod.user_stories_prod.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StoryTemplateService {

    @Value("${app.story-template}")
    private String template;

    public String getTemplate()
        {
        return template;
        }
}
