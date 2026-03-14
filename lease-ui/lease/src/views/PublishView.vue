<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showMessage } from '@/utils/message' 
import { getCategoriesAPI, uploadImageAPI, publishMachineryAPI } from '@/api/machinery'

const router = useRouter()

// ================= 1. 动态分类数据 =================
const categoryList = ref([])

const fetchCategories = async () => {
  try {
    const res = await getCategoriesAPI()
    if (res.code === 200) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// ================= 2. 表单核心数据 =================
const formData = reactive({
  name: '',
  brand: '',
  model: '',
  categoryId: '',
  price: null,
  deposit: null,
  description: '',
  // 核心参数默认给两项
  parameters: [
    { key: '马力', value: '' },
    { key: '驱动', value: '' }
  ]
})

// 动态参数增删
const addParameter = () => {
  if (formData.parameters.length >= 8) {
    showMessage('最多只能添加 8 个核心参数', 'warning')
    return
  }
  formData.parameters.push({ key: '', value: '' })
}
const removeParameter = (index) => {
  formData.parameters.splice(index, 1)
}

// ================= 3. 图片上传逻辑 =================
const uploadedImages = ref([]) 
const fileInputRef = ref(null)
const isUploading = ref(false)

const triggerUpload = () => {
  if (uploadedImages.value.length >= 5) {
    showMessage('最多只能上传 5 张图片', 'warning')
    return
  }
  fileInputRef.value.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    showMessage('只能上传图片格式文件', 'error')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    showMessage('图片大小不能超过 5MB', 'error')
    return
  }

  isUploading.value = true
  try {
    const res = await uploadImageAPI(file)
    if (res.code === 200) {
      uploadedImages.value.push(res.data)
      showMessage('图片上传成功', 'success')
    } else {
      showMessage(res.message, 'error')
    }
  } catch (error) {
    console.error('上传异常:', error)
    showMessage('上传失败，网络异常', 'error')
  } finally {
    isUploading.value = false
    e.target.value = '' 
  }
}

const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
}

// ================= 4. 提交表单 =================
const isSubmitting = ref(false)

const submitForm = async () => {
  if (uploadedImages.value.length === 0) {
    showMessage('请至少上传一张设备主图', 'error')
    return
  }
  const userInfoStr = localStorage.getItem('user_info')
  if (!userInfoStr) {
    showMessage('登录已过期，请重新登录', 'error')
    router.push('/login')
    return
  }

  const parsedUser = JSON.parse(userInfoStr)
  
  const paramsObj = {}
  formData.parameters.forEach(p => {
    if (p.key && String(p.key).trim() !== '') {
      paramsObj[String(p.key).trim()] = p.value ? String(p.value).trim() : ''
    }
  })

  const payload = {
    name: formData.name,
    brand: formData.brand,
    model: formData.model,
    categoryId: formData.categoryId,
    price: Number(formData.price),     
    deposit: Number(formData.deposit), 
    description: formData.description,
    ownerId: parsedUser.userId,
    images: uploadedImages.value.join(','), 
    parameters: Object.keys(paramsObj).length > 0 ? JSON.stringify(paramsObj) : null
  }

  isSubmitting.value = true
  try {
    const res = await publishMachineryAPI(payload)
    if (res.code === 200) {
      showMessage('农机发布成功！平台审核中...', 'success')
      setTimeout(() => {
        router.push('/') 
      }, 1500)
    } else {
      showMessage(res.message, 'error')
    }
  } catch (error) {
    console.error('发布异常:', error)
    showMessage('发布失败，请稍后重试', 'error')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  window.scrollTo({ top: 0 })
  fetchCategories()
})
</script>

<template>
  <div class="publish-page-compact">
    <div class="wide-container">
      
      <div class="compact-header">
        <h2>发布闲置农机</h2>
        <p>一页极速填写，让您的闲置设备迅速变现</p>
      </div>

      <div class="form-card">
        <form @submit.prevent="submitForm">
          
          <div class="form-grid-layout">
            
            <div class="left-pane">
              <div class="pane-title"><i class="icon">🚜</i> 基础与财务信息</div>
              
              <div class="input-grid">
                <div class="input-item span-full">
                  <label>农机名称 <span class="req">*</span></label>
                  <input type="text" v-model="formData.name" placeholder="如：东方红LX2204轮式拖拉机" required />
                </div>

                <div class="input-item">
                  <label>设备分类 <span class="req">*</span></label>
                  <select v-model="formData.categoryId" required>
                    <option value="" disabled>选择分类</option>
                    <option v-for="cat in categoryList" :key="cat.id" :value="cat.id">
                      {{ cat.name }}
                    </option>
                  </select>
                </div>
                <div class="input-item">
                  <label>品牌 <span class="req">*</span></label>
                  <input type="text" v-model="formData.brand" placeholder="如：东方红" required />
                </div>
                <div class="input-item">
                  <label>型号 <span class="req">*</span></label>
                  <input type="text" v-model="formData.model" placeholder="如：LX2204" required />
                </div>

                <div class="input-item span-half">
                  <label>日租金 (元/天) <span class="req">*</span></label>
                  <div class="prefix-input">
                    <span class="prefix">￥</span>
                    <input type="number" v-model="formData.price" placeholder="0.00" min="0" step="0.01" required />
                  </div>
                </div>
                <div class="input-item span-half">
                  <label>押金 (元) <span class="req">*</span></label>
                  <div class="prefix-input">
                    <span class="prefix">￥</span>
                    <input type="number" v-model="formData.deposit" placeholder="0.00" min="0" step="0.01" required />
                  </div>
                </div>

                <div class="input-item span-full">
                  <label>详细描述</label>
                  <textarea v-model="formData.description" placeholder="请描述设备的使用年限、作业效率、磨损情况等..."></textarea>
                </div>
              </div>
            </div>

            <div class="right-pane">
              
              <div class="pane-title"><i class="icon">📷</i> 设备实拍图 <span class="req">*</span></div>
              
              <div class="upload-gallery">
                <div class="image-thumbnail" v-for="(img, index) in uploadedImages" :key="index">
                  <img :src="img" alt="实拍图" />
                  <div class="delete-mask" @click="removeImage(index)">
                    <span class="delete-icon">🗑️</span>
                  </div>
                </div>

                <div v-if="uploadedImages.length < 5" class="upload-trigger" @click="triggerUpload" :class="{ 'is-loading': isUploading }">
                  <span v-if="!isUploading" class="plus-icon">+</span>
                  <div v-else class="spinner-small"></div>
                  <span class="upload-text">{{ isUploading ? '上传中...' : '上传图片' }}</span>
                </div>
                <input type="file" ref="fileInputRef" style="display: none" accept="image/*" @change="handleFileChange" />
              </div>
              <div class="upload-hint">最多可上传 5 张图片，单张不超过 5MB</div>


              <div class="pane-title mt-20">
                <span><i class="icon">⚙️</i> 核心配置参数</span>
                <span class="add-param-btn" @click="addParameter">+ 添加参数</span>
              </div>
              
              <div class="params-scroll-area">
                <div class="param-row" v-for="(param, index) in formData.parameters" :key="index">
                  <input type="text" v-model="param.key" placeholder="参数名" class="param-input" />
                  <span class="divider">-</span>
                  <input type="text" v-model="param.value" placeholder="参数值" class="param-input" />
                  <button type="button" class="btn-remove" @click="removeParameter(index)" title="删除">✕</button>
                </div>
                <div v-if="formData.parameters.length === 0" class="empty-param-hint">暂未添加具体参数</div>
              </div>

            </div>
          </div>

          <div class="form-footer">
            <div class="agreement">
              <input type="checkbox" id="agree" required />
              <label for="agree">我已阅读并同意 <a href="#">《平台设备出租规范》</a>，承诺所填信息真实有效。</label>
            </div>
            <button type="submit" class="submit-btn" :disabled="isSubmitting">
              {{ isSubmitting ? '正在提交...' : '立即发布上架' }}
            </button>
          </div>

        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.publish-page-compact {
  min-height: calc(100vh - 80px); 
  background-color: #f3f4f6; 
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 20px 60px 20px; 
}

.wide-container { width: 100%; max-width: 1000px; }

.compact-header { display: flex; align-items: baseline; gap: 15px; margin-bottom: 20px; padding-left: 5px; }
.compact-header h2 { margin: 0; font-size: 1.4rem; font-weight: 800; color: var(--text-dark); }
.compact-header p { margin: 0; font-size: 0.9rem; color: var(--text-medium); }

.form-card { background: #ffffff; border-radius: 12px; padding: 35px 40px; box-shadow: 0 4px 20px rgba(0,0,0,0.04); }

.form-grid-layout { display: grid; grid-template-columns: 1.5fr 1fr; gap: 50px; }

.pane-title { font-size: 1.05rem; font-weight: 700; color: var(--text-dark); margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; }
.pane-title .icon { font-size: 1.2rem; margin-right: 6px; }
.mt-20 { margin-top: 30px; }

.input-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.span-full { grid-column: 1 / -1; }
.span-half { grid-column: span 1.5; } 

.input-item { display: flex; flex-direction: column; gap: 8px; }
.input-item label { font-size: 0.9rem; font-weight: 600; color: #475569; }
.req { color: #ef4444; margin-left: 2px; }

input[type="text"], input[type="number"], select, textarea { width: 100%; padding: 10px 14px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.95rem; color: var(--text-dark); background-color: #fdfdfd; transition: 0.2s; }
input:focus, select:focus, textarea:focus { outline: none; border-color: var(--primary-color); background-color: #ffffff; box-shadow: 0 0 0 3px rgba(84, 101, 255, 0.1); }
textarea { resize: vertical; min-height: 90px; }

.prefix-input { position: relative; display: flex; align-items: center; }
.prefix-input .prefix { position: absolute; left: 14px; color: #94a3b8; font-weight: bold; }
.prefix-input input { padding-left: 35px; }

.upload-gallery { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 8px; }
.image-thumbnail { aspect-ratio: 1 / 1; border-radius: 8px; overflow: hidden; position: relative; border: 1px solid #e2e8f0; }
.image-thumbnail img { width: 100%; height: 100%; object-fit: cover; }
.delete-mask { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; opacity: 0; transition: 0.2s; cursor: pointer; }
.image-thumbnail:hover .delete-mask { opacity: 1; }
.delete-icon { font-size: 1.2rem; color: white; }

.upload-trigger { aspect-ratio: 1 / 1; border: 2px dashed #cbd5e1; border-radius: 8px; background: #f8fafc; display: flex; flex-direction: column; justify-content: center; align-items: center; cursor: pointer; transition: 0.2s; }
.upload-trigger:hover { border-color: var(--primary-color); background: #f5f7ff; }
.plus-icon { font-size: 1.8rem; color: #94a3b8; margin-bottom: -5px; }
.upload-text { font-size: 0.75rem; color: #94a3b8; margin-top: 5px; font-weight: 600;}
.upload-hint { font-size: 0.75rem; color: #94a3b8; margin-top: 5px; }

.spinner-small { width: 20px; height: 20px; border: 2px solid #cbd5e1; border-top-color: var(--primary-color); border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.add-param-btn { font-size: 0.85rem; color: var(--primary-color); cursor: pointer; font-weight: 600; }
.add-param-btn:hover { text-decoration: underline; }

.params-scroll-area { max-height: 220px; overflow-y: auto; padding-right: 5px; display: flex; flex-direction: column; gap: 12px; }
.params-scroll-area::-webkit-scrollbar { width: 4px; }
.params-scroll-area::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 4px; }

/* 🌟 核心修复：强制等宽且防止被内容挤压变形 */
.param-row { display: flex; align-items: center; gap: 12px; }
.param-input { flex: 1; min-width: 0; } 
.divider { color: #cbd5e1; font-weight: bold; }
.btn-remove { background: #f1f5f9; border: none; color: #64748b; width: 32px; height: 32px; border-radius: 6px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; font-size: 0.9rem; flex-shrink: 0; }
.btn-remove:hover { background: #fee2e2; color: #ef4444; }
.empty-param-hint { font-size: 0.85rem; color: #94a3b8; text-align: center; padding: 20px 0; background: #f8fafc; border-radius: 6px; }

.form-footer { margin-top: 40px; padding-top: 25px; border-top: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; }
.agreement { display: flex; align-items: center; gap: 8px; font-size: 0.9rem; color: var(--text-dark); }
.agreement input { width: 18px; height: 18px; accent-color: var(--primary-color); cursor: pointer;}
.agreement a { color: var(--primary-color); font-weight: 600; }
.agreement a:hover { text-decoration: underline; }

.submit-btn { background: var(--primary-color); color: white; border: none; padding: 12px 35px; font-size: 1.05rem; font-weight: 700; border-radius: 8px; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 15px rgba(84, 101, 255, 0.25); }
.submit-btn:hover:not(:disabled) { background: var(--hover-black); transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15); }
.submit-btn:disabled { background: #94a3b8; cursor: not-allowed; box-shadow: none; }

@media (max-width: 900px) { .form-grid-layout { grid-template-columns: 1fr; gap: 30px; } .form-footer { flex-direction: column; gap: 20px; align-items: stretch; } .submit-btn { width: 100%; } }
</style>