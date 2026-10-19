package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("coach")
public class Coach {

    @TableId(type = IdType.AUTO)
    private Long coachId;

    private Long trainId;

    private Integer coachNo;

}