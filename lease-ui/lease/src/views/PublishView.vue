<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showMessage } from '@/utils/message' // 如果你上次加了全局提示，这里可以直接用

const router = useRouter()

// 表单数据绑定
const formData = reactive({
  name: '',
  brand: '',
  model: '',
  categoryId: '',
  price: null,
  deposit: null,
  description: '',
  parameters: [
    { key: '马力', value: '' },
    { key: '驱动', value: '' }
  ]
})

const addParameter = () => {
  formData.parameters.push({ key: '', value: '' })
}

const removeParameter = (index) => {
  formData.parameters.splice(index, 1)
}

const submitForm = () => {
  console.log('提交的农机数据:', formData)
  // 使用之前写的弹窗，或者保留 alert
  if (typeof showMessage === 'function') {
    showMessage('农机发布成功！(模拟)', 'success')
  } else {
    alert('农机发布成功！(模拟)')
  }
  router.push('/profile')
}
</script>

<template>
  <div class="publish-page-full">
    <div class="wide-container">
      
      <header class="page-header">
        <h1>发布农机设备</h1>
        <p>将您的闲置农机共享出去，获取丰厚收益，助力现代农业。</p>
      </header>

      <form class="wide-form" @submit.prevent="submitForm">
        
        <div class="form-row">
          <div class="row-sidebar">
            <h3><span class="icon">🚜</span> 基本信息</h3>
            <p>请准确填写您的农机基础属性，详尽的信息有助于租客更快地检索并匹配到您的设备。</p>
          </div>
          <div class="row-content">
            <div class="input-group full-width">
              <label>农机名称 <span class="required">*</span></label>
              <input type="text" v-model="formData.name" placeholder="例如：东方红LX2204轮式拖拉机" required />
            </div>
            <div class="grid-2">
              <div class="input-group">
                <label>品牌 <span class="required">*</span></label>
                <input type="text" v-model="formData.brand" placeholder="例如：东方红" required />
              </div>
              <div class="input-group">
                <label>型号 <span class="required">*</span></label>
                <input type="text" v-model="formData.model" placeholder="例如：LX2204" required />
              </div>
            </div>
            <div class="input-group full-width">
              <label>设备分类 <span class="required">*</span></label>
              <select v-model="formData.categoryId" required>
                <option value="" disabled>请选择农机分类</option>
                <option value="1">拖拉机</option>
                <option value="2">收割机</option>
                <option value="3">植保无人机</option>
                <option value="4">农具及配件</option>
              </select>
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="row-sidebar">
            <h3><span class="icon">💰</span> 租赁设置</h3>
            <p>合理设置您的日租金和押金。押金将在租赁结束后，确认设备无损后退还给租客。</p>
          </div>
          <div class="row-content">
            <div class="grid-2">
              <div class="input-group">
                <label>日租金 (元/天) <span class="required">*</span></label>
                <div class="input-with-prefix">
                  <span class="prefix">￥</span>
                  <input type="number" v-model="formData.price" placeholder="0.00" min="0" step="0.01" required />
                </div>
              </div>
              <div class="input-group">
                <label>押金 (元) <span class="required">*</span></label>
                <div class="input-with-prefix">
                  <span class="prefix">￥</span>
                  <input type="number" v-model="formData.deposit" placeholder="0.00" min="0" step="0.01" required />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="row-sidebar">
            <h3><span class="icon">📝</span> 详细描述与参数</h3>
            <p>详细描述农机的使用年限、作业效率、维修记录等。您可以添加多个自定义的核心参数。</p>
          </div>
          <div class="row-content">
            <div class="input-group full-width">
              <label>设备描述</label>
              <textarea v-model="formData.description" rows="4" placeholder="请描述您的设备亮点及租赁注意事项..."></textarea>
            </div>

            <div class="dynamic-params mt-20">
              <label>核心参数配置</label>
              <div class="param-row" v-for="(param, index) in formData.parameters" :key="index">
                <input type="text" v-model="param.key" placeholder="参数名 (如: 马力)" class="param-input" />
                <span class="divider">-</span>
                <input type="text" v-model="param.value" placeholder="参数值 (如: 220HP)" class="param-input" />
                <button type="button" class="btn-remove" @click="removeParameter(index)" title="删除参数">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
                </button>
              </div>
              <button type="button" class="btn-add-param" @click="addParameter">+ 新增一项参数</button>
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="row-sidebar">
            <h3><span class="icon">📷</span> 设备图片</h3>
            <p>上传真实、清晰的设备照片能大幅提升您的出租成功率。建议横屏拍摄。</p>
          </div>
          <div class="row-content">
            <div class="upload-area">
              <div class="upload-placeholder">
                <svg class="upload-icon" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                <p class="main-text">点击或将图片拖拽到此处上传</p>
                <p class="sub-text">支持 JPG/PNG 格式，最大 5MB</p>
              </div>
            </div>
          </div>
        </div>

        <div class="submit-bar">
          <div class="submit-content">
            <p class="agreement">发布即代表您同意 <a href="#">《平台租赁服务协议》</a></p>
            <button type="submit" class="btn-primary large-btn">确认发布出租</button>
          </div>
        </div>

      </form>
    </div>
  </div>
</template>

<style scoped>
/* ================= 整体页面背景 ================= */
.publish-page-full {
  min-height: calc(100vh - 80px);
  background-color: #ffffff; /* 纯白背景，非常干净 */
  padding: 60px 0 100px;
}

/* ================= 页面头部 ================= */
.page-header {
  margin-bottom: 50px;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 30px;
}
.page-header h1 {
  font-size: 2.2rem;
  font-weight: 800;
  color: var(--text-dark);
  margin-bottom: 10px;
}
.page-header p {
  font-size: 1.1rem;
  color: var(--text-medium);
}

/* ================= 左右分栏表单行 ================= */
.wide-form {
  display: flex;
  flex-direction: column;
}

.form-row {
  display: flex;
  gap: 80px; /* 左右两栏的间距 */
  padding: 40px 0;
  border-bottom: 1px solid #f1f5f9; /* 行与行之间的分割线 */
}

/* 左侧标题与描述区 */
.row-sidebar {
  width: 320px; /* 左侧固定宽度 */
  flex-shrink: 0;
}
.row-sidebar h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-dark);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.row-sidebar h3 .icon { font-size: 1.4rem; }
.row-sidebar p {
  font-size: 0.95rem;
  color: var(--text-medium);
  line-height: 1.6;
}

/* 右侧输入区 */
.row-content {
  flex: 1; /* 占据剩余全部宽度 */
  max-width: 800px; /* 限制一下最大宽度，防止带鱼屏下输入框过长 */
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ================= 输入框与控件 ================= */
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.input-group { display: flex; flex-direction: column; gap: 8px; }
.full-width { grid-column: 1 / -1; }

label { font-size: 0.9rem; font-weight: 600; color: var(--text-dark); }
.required { color: #ef4444; margin-left: 4px; }

input[type="text"], input[type="number"], select, textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  color: var(--text-dark);
  background-color: #f8fafc; /* 极浅的灰色底，区分白色的页面背景 */
  transition: all 0.3s ease;
}
input:focus, select:focus, textarea:focus { 
  outline: none; 
  border-color: var(--primary-color); 
  background-color: white; 
  box-shadow: 0 0 0 3px rgba(84, 101, 255, 0.1); 
}
textarea { resize: vertical; min-height: 100px; }

/* 带有前缀的输入框 (如 ￥ 符号) */
.input-with-prefix {
  position: relative;
  display: flex;
  align-items: center;
}
.input-with-prefix .prefix {
  position: absolute;
  left: 16px;
  color: var(--text-medium);
  font-weight: 600;
}
.input-with-prefix input {
  padding-left: 35px; /* 给前缀留出空间 */
}

/* 动态参数项 */
.mt-20 { margin-top: 20px; }
.dynamic-params { display: flex; flex-direction: column; gap: 12px; }
.param-row { display: flex; align-items: center; gap: 12px; }
.param-input { flex: 1; }
.divider { color: #cbd5e1; font-weight: bold; }
.btn-remove { background: transparent; border: none; color: #94a3b8; cursor: pointer; padding: 6px; border-radius: 6px; transition: 0.2s; display: flex; align-items: center; justify-content: center; }
.btn-remove svg { width: 20px; height: 20px; }
.btn-remove:hover { background: #fee2e2; color: #ef4444; }

.btn-add-param { background: transparent; border: 1px dashed #cbd5e1; color: var(--primary-color); padding: 12px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.3s; margin-top: 5px; }
.btn-add-param:hover { border-color: var(--primary-color); background: rgba(84,101,255,0.05); }

/* 图片上传区 */
.upload-area { border: 2px dashed #cbd5e1; border-radius: 12px; padding: 50px 20px; text-align: center; background: #f8fafc; cursor: pointer; transition: 0.3s; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.upload-area:hover { border-color: var(--primary-color); background: #f5f7ff; }
.upload-icon { width: 48px; height: 48px; color: #94a3b8; margin-bottom: 15px; }
.upload-placeholder .main-text { font-size: 1.05rem; font-weight: 600; color: var(--text-dark); margin-bottom: 6px; }
.upload-placeholder .sub-text { font-size: 0.85rem; color: #94a3b8; }

/* 底部提交栏 */
.submit-bar {
  margin-top: 50px;
  display: flex;
  justify-content: flex-end; /* 按钮靠右对齐 */
}
.submit-content {
  display: flex;
  align-items: center;
  gap: 25px;
}
.agreement { font-size: 0.9rem; color: var(--text-medium); }
.agreement a { color: var(--primary-color); font-weight: 600; }
.agreement a:hover { text-decoration: underline; }

.large-btn { padding: 14px 35px; font-size: 1.1rem; border-radius: 8px; box-shadow: 0 4px 15px rgba(84, 101, 255, 0.3); }

/* ================= 响应式适配 ================= */
@media (max-width: 1024px) {
  .form-row { flex-direction: column; gap: 20px; padding: 30px 0; }
  .row-sidebar { width: 100%; }
  .row-content { max-width: 100%; }
  .submit-bar { justify-content: center; }
  .submit-content { flex-direction: column-reverse; gap: 15px; width: 100%; }
  .large-btn { width: 100%; }
}

@media (max-width: 768px) {
  .grid-2 { grid-template-columns: 1fr; }
  .publish-page-full { padding: 30px 0 60px; }
}
</style>