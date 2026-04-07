<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminUsersAPI, changeUserStatusAPI, getAdminMachineryAPI, updateMachineryPriceAPI } from '@/api/admin'
import { showMessage } from '@/utils/message'
import { showConfirm } from '@/utils/confirm'

const router = useRouter()
const currentTab = ref('users') // users | machinery

const userList = ref([])
const machineryList = ref([])

// 提取封面图
const getCoverImage = (imagesStr) => {
  if (!imagesStr) return 'https://via.placeholder.com/150x100?text=暂无图片'
  return imagesStr.split(',')[0]
}

// 拉取用户列表
const fetchUsers = async () => {
  try {
    const res = await getAdminUsersAPI()
    if (res.code === 200) userList.value = res.data
  } catch (e) { console.error(e) }
}

// 拉取农机列表
const fetchMachinery = async () => {
  try {
    const res = await getAdminMachineryAPI()
    if (res.code === 200) machineryList.value = res.data
  } catch (e) { console.error(e) }
}

// 切换账号状态 (停用/启用)
const toggleUserStatus = async (user) => {
  const action = user.status === 1 ? '停用' : '启用'
  const newStatus = user.status === 1 ? 0 : 1
  const isConfirmed = await showConfirm(`确定要 ${action} 账号 [${user.username}] 吗？`)
  if (!isConfirmed) return
  
  try {
    await changeUserStatusAPI(user.id, newStatus)
    showMessage(`${action}成功`, 'success')
    fetchUsers()
  } catch (e) { showMessage('操作失败', 'error') }
}

// 修改价格
const editPrice = async (item) => {
  const newPrice = prompt(`请输入 [${item.name}] 的新价格 (当前: ¥${item.price}):`, item.price)
  if (newPrice === null || newPrice === item.price) return
  if (isNaN(newPrice) || Number(newPrice) <= 0) {
    return showMessage('请输入合法的价格数字', 'error')
  }

  try {
    await updateMachineryPriceAPI(item.id, newPrice)
    showMessage('价格修改成功', 'success')
    fetchMachinery()
  } catch (e) { showMessage('修改失败', 'error') }
}

const handleLogout = () => {
  localStorage.removeItem('user_token')
  localStorage.removeItem('user_info')
  showMessage('管理员已退出', 'success')
  router.push('/login')
}

onMounted(() => {
  fetchUsers()
  fetchMachinery()
})
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">
        <h2>🛠️ 管理员控制台</h2>
      </div>
      <nav class="nav-menu">
        <button :class="['nav-btn', { active: currentTab === 'users' }]" @click="currentTab = 'users'">👥 用户管理</button>
        <button :class="['nav-btn', { active: currentTab === 'machinery' }]" @click="currentTab = 'machinery'">🚜 农机与价格监管</button>
      </nav>
      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">退出控制台</button>
      </div>
    </aside>

    <main class="main-content">
      <div v-if="currentTab === 'users'" class="panel fade-in">
        <h2 class="panel-title">平台用户管理</h2>
        <div class="table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>真实姓名</th>
                <th>手机号</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in userList" :key="user.id">
                <td>{{ user.id }}</td>
                <td><strong>{{ user.username }}</strong></td>
                <td>{{ user.realName || '-' }}</td>
                <td>{{ user.phone }}</td>
                <td>
                  <span :class="['status-tag', user.status === 1 ? 'normal' : 'banned']">
                    {{ user.status === 1 ? '正常' : '已冻结' }}
                  </span>
                </td>
                <td>
                  <button :class="['action-btn', user.status === 1 ? 'danger' : 'success']" @click="toggleUserStatus(user)">
                    {{ user.status === 1 ? '封禁账号' : '解封账号' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="currentTab === 'machinery'" class="panel fade-in">
        <h2 class="panel-title">农机物料与价格监管</h2>
        <div class="table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>实拍图</th>
                <th>设备名称</th>
                <th>所属机主</th>
                <th>日租金(元)</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in machineryList" :key="item.id">
                <td>{{ item.id }}</td>
                <td><img :src="getCoverImage(item.images)" class="table-img" /></td>
                <td><strong>{{ item.name }}</strong></td>
                <td>{{ item.ownerName || '未知' }}</td>
                <td class="price-col">¥{{ item.price }}</td>
                <td>
                  <button class="action-btn edit" @click="editPrice(item)">修改价格</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-layout { display: flex; height: 100vh; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
.sidebar { width: 260px; background: #1e293b; color: white; display: flex; flex-direction: column; }
.brand { padding: 30px 20px; border-bottom: 1px solid #334155; }
.brand h2 { font-size: 1.3rem; margin: 0; color: #f8fafc; }
.nav-menu { padding: 20px 10px; flex: 1; display: flex; flex-direction: column; gap: 10px; }
.nav-btn { background: transparent; border: none; color: #cbd5e1; text-align: left; padding: 15px 20px; border-radius: 8px; font-size: 1rem; cursor: pointer; transition: 0.2s; }
.nav-btn:hover { background: #334155; color: white; }
.nav-btn.active { background: #3b82f6; color: white; font-weight: bold; }
.sidebar-footer { padding: 20px; border-top: 1px solid #334155; }
.logout-btn { width: 100%; background: transparent; border: 1px solid #ef4444; color: #ef4444; padding: 12px; border-radius: 8px; cursor: pointer; font-weight: 600; transition: 0.2s; }
.logout-btn:hover { background: #ef4444; color: white; }

.main-content { flex: 1; padding: 40px; overflow-y: auto; }
.panel-title { margin-top: 0; margin-bottom: 25px; color: #0f172a; font-size: 1.5rem; }
.table-container { background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); overflow: hidden; }
.admin-table { width: 100%; border-collapse: collapse; text-align: left; }
.admin-table th { background: #f8fafc; padding: 15px 20px; color: #64748b; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
.admin-table td { padding: 15px 20px; border-bottom: 1px solid #e2e8f0; color: #334155; vertical-align: middle; }
.admin-table tbody tr:hover { background: #fcfcfc; }

.table-img { width: 80px; height: 50px; object-fit: cover; border-radius: 4px; border: 1px solid #e2e8f0; }
.price-col { color: #ef4444 !important; font-weight: bold; font-size: 1.1rem; }

.status-tag { padding: 4px 10px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; }
.status-tag.normal { background: #dcfce7; color: #16a34a; }
.status-tag.banned { background: #fee2e2; color: #dc2626; }

.action-btn { border: none; padding: 8px 15px; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 0.85rem; transition: 0.2s; }
.action-btn.edit { background: #e0e7ff; color: #4f46e5; }
.action-btn.edit:hover { background: #4f46e5; color: white; }
.action-btn.danger { background: #fee2e2; color: #dc2626; }
.action-btn.danger:hover { background: #dc2626; color: white; }
.action-btn.success { background: #dcfce7; color: #16a34a; }
.action-btn.success:hover { background: #16a34a; color: white; }

.fade-in { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>