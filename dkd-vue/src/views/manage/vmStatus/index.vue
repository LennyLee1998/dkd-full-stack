<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="设备编号" prop="innerCode">
        <el-input
          v-model="queryParams.innerCode"
          placeholder="请输入设备编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="vmList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="index" width="55" align="center" label="序号" />
      <el-table-column label="设备编号" align="center" prop="innerCode" />
      <el-table-column label="设备型号" align="center" prop="vmTypeId">
        <template #default="scope">
          <div v-for="item in vmTypeList" :key="item.id">
            <span v-if="item.id === scope.row.vmTypeId">{{ item.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        label="详细地址"
        align="left"
        prop="addr"
        show-overflow-tooltip
      />
      <el-table-column label="运营状态" align="center" prop="vmStatus">
        <template #default="scope">
          <dict-tag :options="vm_status" :value="scope.row.vmStatus" />
        </template>
      </el-table-column>

      <el-table-column label="设备状态" align="center" prop="runningStatus">
        <template #default="scope">
          <span>
            {{ JSON.parse(scope.row.runningStatus)?.status ? "正常" : "异常" }}
          </span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="getVmInfo(scope.row)"
            v-hasPermi="['manage:vm:query']"
            >查看详情</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改设备管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Vm">
import { listVm, getVm, delVm, addVm, updateVm } from "@/api/manage/vm";
import { listPartner } from "@/api/manage/partner";
import { listVmType } from "@/api/manage/vmType";
import { loadAllParams } from "@/api/page";
import { listNode } from "@/api/manage/node";
import { listRegion } from "@/api/manage/region";

const { proxy } = getCurrentInstance();
const { vm_status } = proxy.useDict("vm_status");

const vmList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    innerCode: null,
    nodeId: null,
    businessType: null,
    regionId: null,
    partnerId: null,
    vmTypeId: null,
    vmStatus: null,
    runningStatus: null,
    policyId: null,
  },
  rules: {
    nodeId: [{ required: true, message: "点位Id不能为空", trigger: "blur" }],
    vmTypeId: [
      { required: true, message: "设备型号不能为空", trigger: "blur" },
    ],
  },
});

const { queryParams, form, rules } = toRefs(data);
const partnerList = ref([]);
const vmTypeList = ref([]);
const nodeList = ref([]);
const regionList = ref([]);

/** 查询设备管理列表 */
async function getList() {
  loading.value = true;
  const vmListRes = await listVm(queryParams.value);
  const partnerListRes = await listPartner(loadAllParams);
  const vmTypeListRes = await listVmType(loadAllParams);
  const nodeListRes = await listNode(loadAllParams);
  const regionListRes = await listRegion(loadAllParams);

  vmList.value = vmListRes.rows;
  partnerList.value = partnerListRes.rows;
  vmTypeList.value = vmTypeListRes.rows;
  nodeList.value = nodeListRes.rows;
  regionList.value = regionListRes.rows;
  total.value = vmListRes.total;
  loading.value = false;
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    innerCode: null,
    channelMaxCapacity: null,
    nodeId: null,
    addr: null,
    lastSupplyTime: null,
    businessType: null,
    regionId: null,
    partnerId: null,
    vmTypeId: null,
    vmStatus: null,
    runningStatus: null,
    longitudes: null,
    latitude: null,
    clientId: null,
    policyId: null,
    createTime: null,
    updateTime: null,
  };
  proxy.resetForm("vmRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加设备管理";
}

/** 修改按钮操作 */
function getVmInfo(row) {
  reset();
  const _id = row.id || ids.value;
  getVm(_id).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = "设备详情";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["vmRef"].validate((valid) => {
    if (valid) {
      if (form.value.id != null) {
        updateVm(form.value).then((response) => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addVm(form.value).then((response) => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal
    .confirm('是否确认删除设备管理编号为"' + _ids + '"的数据项？')
    .then(function () {
      return delVm(_ids);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download(
    "manage/vm/export",
    {
      ...queryParams.value,
    },
    `vm_${new Date().getTime()}.xlsx`
  );
}

getList();
</script>
