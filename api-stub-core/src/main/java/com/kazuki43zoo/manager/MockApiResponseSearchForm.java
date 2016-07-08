package com.kazuki43zoo.manager;


import lombok.Data;

import java.io.Serializable;

@Data
class MockApiResponseSearchForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private String path;
    private String description;
}
