package com.dkd.manage.service.impl;

import com.dkd.common.constant.DkdContants;
import com.dkd.common.utils.DateUtils;
import com.dkd.common.utils.uuid.UUIDUtils;
import com.dkd.manage.domain.Channel;
import com.dkd.manage.domain.Node;
import com.dkd.manage.domain.VendingMachine;
import com.dkd.manage.domain.VmType;
import com.dkd.manage.mapper.VendingMachineMapper;
import com.dkd.manage.mapper.VmTypeMapper;
import com.dkd.manage.service.IChannelService;
import com.dkd.manage.service.INodeService;
import com.dkd.manage.service.IVendingMachineService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备管理Service业务层处理
 *
 * @author lenny
 * @date 2025-02-27
 */
@Service
public class VendingMachineServiceImpl implements IVendingMachineService {
  @Autowired
  private VendingMachineMapper vendingMachineMapper;
  @Autowired
  private VmTypeMapper vmTypeMapper;
  @Autowired
  private INodeService nodeService;
  @Autowired
  private IChannelService channelService;


  /**
   * 查询设备管理
   *
   * @param id 设备管理主键
   * @return 设备管理
   */
  @Override
  public VendingMachine selectVendingMachineById(Long id) {
    return vendingMachineMapper.selectVendingMachineById(id);
  }

  /**
   * 查询设备管理列表
   *
   * @param vendingMachine 设备管理
   * @return 设备管理
   */
  @Override
  public List<VendingMachine> selectVendingMachineList(VendingMachine vendingMachine) {
    return vendingMachineMapper.selectVendingMachineList(vendingMachine);
  }

  /**
   * 新增设备管理
   *
   * @param vendingMachine 设备管理
   * @return 结果
   */
  @Transactional
  public int insertVendingMachine(VendingMachine vendingMachine) {
//        1. 新增设备
//    1.1 生成8位的唯一标识, 补充设备编号
    String innerCode = UUIDUtils.getUUID();
    vendingMachine.setInnerCode(innerCode);
    //  1.2 查询vm_type 补充设备容量
    VmType vmType = vmTypeMapper.selectVmTypeById(vendingMachine.getVmTypeId());
    Long maxCapacity = vmType.getChannelMaxCapacity();
    vendingMachine.setChannelMaxCapacity(maxCapacity);
    //    1.3 查询node 补充addr businessType regionId partnerId
    Node node = nodeService.selectNodeById(vendingMachine.getNodeId());
    BeanUtils.copyProperties(node, vendingMachine, "id");
    vendingMachine.setAddr(node.getAddress());
//    1.4 设备状态
    vendingMachine.setVmStatus(DkdContants.VM_STATUS_NODEPLOY); //0表示未投放
    vendingMachine.setCreateTime(DateUtils.getNowDate()); //创建时间
    vendingMachine.setUpdateTime(DateUtils.getNowDate()); //更新时间
    int result = vendingMachineMapper.insertVendingMachine(vendingMachine);

//        2. 新增货道
    Long row = vmType.getVmRow();
    Long col = vmType.getVmCol();
    List<Channel> channelList = new ArrayList<>();
    for (int i = 1; i <= row; i++) {
      for (int j = 1; j <= col; j++) {
        String channelCode = i + "-" + j;
        Channel channel = Channel.builder()
            .channelCode(channelCode)
            .innerCode(innerCode)
            .maxCapacity(maxCapacity)
            .vmId(vendingMachine.getId())
            .build();
        channel.setCreateTime(DateUtils.getNowDate());
        channel.setUpdateTime(DateUtils.getNowDate());
        channelList.add(channel);
      }
    }
    channelService.insertBatch(channelList);
    return result;
  }

  /**
   * 修改设备管理
   *
   * @param vendingMachine 设备管理
   * @return 结果
   */
  @Override
  public int updateVendingMachine(VendingMachine vendingMachine) {
    //查询点位表,补充区域 点位 合作商
    Long nodeId = vendingMachine.getNodeId();
    if (nodeId != null) {
      Node node = nodeService.selectNodeById(nodeId);
      BeanUtils.copyProperties(node, vendingMachine, "id", "createTime"); //商圈类型 区域 合作商
      vendingMachine.setAddr(node.getAddress()); //设备地址
    }
    vendingMachine.setUpdateTime(DateUtils.getNowDate()); //更新时间
    return vendingMachineMapper.updateVendingMachine(vendingMachine);
  }

  /**
   * 批量删除设备管理
   *
   * @param ids 需要删除的设备管理主键
   * @return 结果
   */
  @Override
  public int deleteVendingMachineByIds(Long[] ids) {
    return vendingMachineMapper.deleteVendingMachineByIds(ids);
  }

  /**
   * 删除设备管理信息
   *
   * @param id 设备管理主键
   * @return 结果
   */
  @Override
  public int deleteVendingMachineById(Long id) {
    return vendingMachineMapper.deleteVendingMachineById(id);
  }
}
