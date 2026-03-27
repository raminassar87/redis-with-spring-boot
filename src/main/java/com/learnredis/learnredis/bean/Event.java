package com.learnredis.learnredis.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event implements Serializable {
    private String id;
    private String title;
    private String location;
    private String date;
}