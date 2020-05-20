package com.zz.scservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class OrderInfo {
    private String orderNo;
    private Date payTime;
    private String userId;
    private String port;
    private String issueId;
    private String cardCode;
}
