package com.datacanal.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class DbNode {
    private String host;
    private int port;
    private String username;
    private String password;
    private String dbName;
}
