package org.example.pges.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TextDTO implements Serializable{
    private Long id;

    private String text;
}
