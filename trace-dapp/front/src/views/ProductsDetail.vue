<template>
  <div id="app">
    <el-container>
      <el-header><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="detail-main">
          <!-- 页面标题行 -->
          <div class="page-title-bar">
            <el-button icon="el-icon-arrow-left" size="small" @click="$router.back()" plain style="margin-right:12px;">返回</el-button>
            <h2><i class="el-icon-document"></i> 产品详情</h2>
          </div>

          <!-- 上半部：轮播图 + 信息 -->
          <el-row :gutter="20" class="top-section">
            <el-col :span="10">
              <div class="carousel-card">
                <el-carousel height="380px" trigger="click" v-if="product.images && product.images.length > 0">
                  <el-carousel-item v-for="(img, index) in product.images" :key="index">
                    <img :src="img" style="width: 100%; height: 100%; object-fit: cover;" />
                  </el-carousel-item>
                </el-carousel>
                <div class="no-image" v-else>
                  <i class="el-icon-picture-outline"></i>
                  <span>暂无图片</span>
                </div>
              </div>
            </el-col>
            <el-col :span="14">
              <div class="info-card">
                <div class="info-header">
                  <h3>{{ product.productName }}</h3>
                  <el-tag :type="product.status === '已上架' ? 'success' : product.status === '已下架' ? 'danger' : 'info'" size="medium">{{ product.status || '草稿' }}</el-tag>
                </div>
                <el-descriptions :column="2" border size="medium">
                  <el-descriptions-item label="产品编号"><code class="mono-sm">{{ product.productNo }}</code></el-descriptions-item>
                  <el-descriptions-item label="分类">
                    <el-tag :type="getCategoryTagType(product.category)" size="small">{{ product.category }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="价格"><span class="price-lg">¥{{ product.price }}</span></el-descriptions-item>
                  <el-descriptions-item label="单位">{{ product.unit }}</el-descriptions-item>
                  <el-descriptions-item label="产地" :span="2">{{ product.originAddress }}</el-descriptions-item>
                  <el-descriptions-item label="扫码次数">{{ product.scanCount || 0 }} 次</el-descriptions-item>
                  <el-descriptions-item label="创建时间">{{ product.createTime }}</el-descriptions-item>
                  <el-descriptions-item label="二维码" v-if="product.qrCode">
                    <img :src="product.qrCode" style="width: 80px; height: 80px; border-radius: 6px; border: 1px solid #eee;" />
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-col>
          </el-row>

          <!-- 下半部：标签页 -->
          <div class="tabs-card">
            <el-tabs v-model="activeTab" type="border-card">
              <el-tab-pane name="planting">
                <template slot="label"><i class="el-icon-s-promotion"></i> 种植记录</template>
                <div v-if="plantingRecords.length > 0">
                  <el-table :data="plantingRecords" border stripe style="width: 100%" size="medium">
                    <el-table-column prop="baseName" label="种植基地" min-width="120"></el-table-column>
                    <el-table-column prop="variety" label="品种" width="120"></el-table-column>
                    <el-table-column prop="sowingDate" label="播种日期" width="120"></el-table-column>
                    <el-table-column prop="area" label="面积(亩)" width="100"></el-table-column>
                    <el-table-column label="农事操作" min-width="200">
                      <template slot-scope="scope">
                        <el-collapse accordion>
                          <el-collapse-item v-for="(op, idx) in (scope.row.farmingOps || [])" :key="idx" :title="op.type + ' - ' + op.date" :name="'' + idx">
                            <div>{{ op.description }}</div>
                          </el-collapse-item>
                          <div v-if="!scope.row.farmingOps || scope.row.farmingOps.length === 0" style="color:#999;padding:8px;">暂无农事操作记录</div>
                        </el-collapse>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
                <el-empty description="暂无种植记录" v-else></el-empty>
              </el-tab-pane>

              <el-tab-pane name="processing">
                <template slot="label"><i class="el-icon-s-operation"></i> 加工记录</template>
                <div v-if="processingRecords.length > 0">
                  <el-timeline>
                    <el-timeline-item v-for="(record, idx) in processingRecords" :key="idx" :timestamp="record.processDate" placement="top" color="#409EFF">
                      <template v-if="parseSteps(record.description).length > 0">
                        <el-card shadow="hover" v-for="(step, sIdx) in parseSteps(record.description)" :key="sIdx" style="margin-bottom:10px;">
                          <h4>{{ step.stepName }}</h4>
                          <p>{{ step.description }}</p>
                          <p style="color:#999;font-size:13px;">负责人：{{ step.operator || '未填写' }}</p>
                        </el-card>
                      </template>
                      <el-card shadow="hover" v-else>
                        <h4>{{ record.stepName || '加工记录' }}</h4>
                        <p>{{ record.description }}</p>
                        <p style="color:#999;font-size:13px;">负责人：{{ record.operator || '未填写' }}</p>
                      </el-card>
                    </el-timeline-item>
                  </el-timeline>
                </div>
                <el-empty description="暂无加工记录" v-else></el-empty>
              </el-tab-pane>

              <el-tab-pane name="testing">
                <template slot="label"><i class="el-icon-success"></i> 检测报告</template>
                <div v-if="testingRecords.length > 0">
                  <el-table :data="testingRecords" border stripe style="width:100%" size="medium">
                    <el-table-column prop="reportName" label="报告名称" min-width="140"></el-table-column>
                    <el-table-column prop="testingOrg" label="检测机构" width="160"></el-table-column>
                    <el-table-column prop="testingDate" label="检测日期" width="120"></el-table-column>
                    <el-table-column prop="result" label="结果" width="90" align="center">
                      <template slot-scope="scope">
                        <el-tag :type="scope.row.result === '合格' ? 'success' : 'danger'" size="small">{{ scope.row.result }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="90" align="center">
                      <template slot-scope="scope"><el-button type="text" @click="downloadReport(scope.row)">下载</el-button></template>
                    </el-table-column>
                  </el-table>
                  <el-table :data="testingItems" border stripe style="width:100%;margin-top:16px" v-if="testingItems.length > 0" size="small">
                    <el-table-column prop="itemName" label="检测项" width="140"></el-table-column>
                    <el-table-column prop="standardValue" label="标准值" width="140"></el-table-column>
                    <el-table-column prop="actualValue" label="检测值" width="140"></el-table-column>
                    <el-table-column prop="qualified" label="是否合格" width="100" align="center">
                      <template slot-scope="scope">
                        <el-tag :type="scope.row.qualified ? 'success' : 'danger'" size="small">{{ scope.row.qualified ? '合格' : '不合格' }}</el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
                <el-empty description="暂无检测报告" v-else></el-empty>
              </el-tab-pane>

              <el-tab-pane name="logistics">
                <template slot="label"><i class="el-icon-truck"></i> 物流轨迹</template>
                <div v-if="logisticsRecords.length > 0">
                  <el-timeline>
                    <el-timeline-item v-for="(item, index) in logisticsRecords" :key="index" :timestamp="item.operateTime" placement="top" :color="item.type === '签收' ? '#67c23a' : '#409EFF'">
                      <el-card shadow="hover">
                        <h4>{{ item.type }}</h4>
                        <p>{{ item.location }} - {{ item.description }}</p>
                        <p style="color:#999;font-size:13px;">操作人：{{ item.operator }}</p>
                      </el-card>
                    </el-timeline-item>
                  </el-timeline>
                </div>
                <el-empty description="暂无物流轨迹" v-else></el-empty>
              </el-tab-pane>

              <el-tab-pane name="verify">
                <template slot="label"><i class="el-icon-key"></i> 溯源验证</template>
                <el-row :gutter="12" style="margin-bottom:16px;">
                  <el-col :span="16">
                    <el-input v-model="verifyCode" placeholder="请输入溯源验证码" clearable size="medium"></el-input>
                  </el-col>
                  <el-col :span="4">
                    <el-button type="primary" @click="handleVerify" size="medium" style="width:100%">查询验证</el-button>
                  </el-col>
                </el-row>
                <div v-if="verifyResult">
                  <el-alert :title="verifyResult.valid ? '验证通过：产品信息真实有效' : '验证失败：溯源信息不匹配'" :type="verifyResult.valid ? 'success' : 'error'" show-icon style="margin-bottom:16px;border-radius:8px;"></el-alert>
                  <el-descriptions :column="2" border v-if="verifyResult.valid" size="medium">
                    <el-descriptions-item label="产品名称">{{ verifyResult.productName }}</el-descriptions-item>
                    <el-descriptions-item label="产品编号">{{ verifyResult.productNo }}</el-descriptions-item>
                    <el-descriptions-item label="产地">{{ verifyResult.originAddress }}</el-descriptions-item>
                    <el-descriptions-item label="上链时间">{{ verifyResult.blockTime }}</el-descriptions-item>
                  </el-descriptions>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import Navigator from '@/components/Navigator';
import Header from '@/components/Header';
export default {
  name: 'ProductsDetail', components: { Navigator, Header },
  data() {
    return { activeTab: 'planting', product: {}, plantingRecords: [], processingRecords: [], testingRecords: [], testingItems: [], logisticsRecords: [], verifyCode: '', verifyResult: null };
  },
  methods: {
    getCategoryTagType(category) { const m={'粮食':'','蔬菜':'success','水果':'warning','肉禽':'danger','水产':'info'}; return m[category]||''; },
    parseSteps(desc) {
      if (!desc) return [];
      try { const parsed = JSON.parse(desc); return Array.isArray(parsed) ? parsed : []; }
      catch (e) { return [{ stepName: '加工步骤', description: desc, operator: '' }]; }
    },
    query() {
      const id = this.$route.params.id; if (!id) return;
      this.axios.get('/api/products/'+id,{headers:{Authorization:'Bearer '+this.$cookies.get('token')}}).then(r=>{if(r.data.code==200){var d=r.data.data||{};this.product=d.product||{};}});
      this.axios.get('/api/products/'+id+'/planting',{headers:{Authorization:'Bearer '+this.$cookies.get('token')}}).then(r=>{if(r.data.code==200)this.plantingRecords=r.data.data||[];});
      this.axios.get('/api/products/'+id+'/processing',{headers:{Authorization:'Bearer '+this.$cookies.get('token')}}).then(r=>{if(r.data.code==200)this.processingRecords=r.data.data||[];});
      this.axios.get('/api/products/'+id+'/testing',{headers:{Authorization:'Bearer '+this.$cookies.get('token')}}).then(r=>{if(r.data.code==200){this.testingRecords=r.data.data.records||r.data.data||[];this.testingItems=r.data.data.items||[];}});
      this.axios.get('/api/products/'+id+'/logistics',{headers:{Authorization:'Bearer '+this.$cookies.get('token')}}).then(r=>{if(r.data.code==200)this.logisticsRecords=r.data.data||[];});
    },
    handleVerify() {
      if(!this.verifyCode){this.$message.warning('请输入溯源验证码');return;}
      const id=this.$route.params.id;
      this.axios.get('/api/products/'+id+'/verify',{params:{code:this.verifyCode},headers:{Authorization:'Bearer '+this.$cookies.get('token')}}).then(r=>{
        if(r.data.code==200)this.verifyResult=r.data.data;else this.verifyResult={valid:false};
      });
    },
    downloadReport(row){
      const id=this.$route.params.id;
      this.axios.get('/api/products/'+id+'/testing/'+row.id+'/download',{headers:{Authorization:'Bearer '+this.$cookies.get('token')},responseType:'blob'}).then(r=>{
        const url=window.URL.createObjectURL(new Blob([r.data]));const link=document.createElement('a');link.href=url;link.setAttribute('download',row.reportName||'report.pdf');document.body.appendChild(link);link.click();document.body.removeChild(link);
      });
    }
  },
  mounted(){this.query();}
};
</script>

<style scoped>
.detail-main { padding: 20px !important; }
.page-title-bar { display: flex; align-items: center; margin-bottom: 16px; }
.page-title-bar h2 { font-size: 20px; font-weight: 600; color: var(--text-primary); margin: 0; display: flex; align-items: center; gap: 8px; }
.page-title-bar h2 i { color: var(--primary); }
.top-section { margin-bottom: 20px; }
.carousel-card { border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); height: 380px; }
.no-image { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; background: #fafbfc; color: #c0c4cc; }
.no-image i { font-size: 48px; margin-bottom: 12px; }
.info-card { background: #fff; border-radius: var(--radius-md); padding: 20px 24px; box-shadow: var(--shadow-sm); height: 380px; overflow-y: auto; }
.info-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.info-header h3 { font-size: 18px; font-weight: 600; color: var(--text-primary); margin: 0; }
.tabs-card { border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.mono-sm { font-family: "SFMono-Regular",Consolas,"Liberation Mono",Menlo,monospace; font-size:12px; background:#f5f7fa; padding:2px 6px; border-radius:4px; }
.price-lg { font-size: 18px; font-weight: 700; color: #E6A23C; }
</style>
