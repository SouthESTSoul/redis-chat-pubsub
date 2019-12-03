package guhao.entity;

import lombok.Data;

import java.io.Serializable;


@Data
public class User implements Serializable {

    private Long id;

    private String name;

    private String avatar;

    public void setName(String name) {
        this.name = name.trim();
    }
}
