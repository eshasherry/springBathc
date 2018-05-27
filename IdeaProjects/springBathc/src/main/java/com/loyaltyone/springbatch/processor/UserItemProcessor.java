package com.loyaltyone.springbatch.processor;

import com.loyaltyone.springbatch.model.User;
import org.springframework.batch.item.ItemProcessor;

/**
 * Created by esherry on 2018-05-27.
 */
public class UserItemProcessor implements ItemProcessor<User, User> {

    @Override
    public User process(User user)throws Exception{
        return user;
    }
}
