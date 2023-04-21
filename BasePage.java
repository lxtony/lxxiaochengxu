package com.dp.common.control;

import lombok.Data;

@Data
public class BasePage {
    int pageNo = 1;
    int pageSize = 20;
    int startRow ;

    public int getPageSize()
    {
        if(pageSize < 0){
            pageSize = 1;
        }
        if(pageSize > 2000){
            pageSize = 2000;
        }
        return pageSize;
    }

    public int getStartRow()
    {
        if(pageNo < 1){
            pageNo = 1;
        }
        return (pageNo-1)*pageSize;
    }
}
