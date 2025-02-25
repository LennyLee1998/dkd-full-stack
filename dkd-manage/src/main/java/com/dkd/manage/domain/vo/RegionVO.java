package com.dkd.manage.domain.vo;

import com.dkd.manage.domain.Region;
import lombok.Data;

import java.util.List;

@Data
public class RegionVO extends Region {

//  @Excel(name = "点位数量")
  private Integer nodeCount;


}
