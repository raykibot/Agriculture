<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  title: { type: String, default: '操作确认' },
  content: { type: String, required: true },
  onConfirm: { type: Function },
  onCancel: { type: Function },
  remove: { type: Function } // 用于销毁 DOM 的回调
})

const visible = ref(false)

onMounted(() => {
  visible.value = true
})

const handleClose = (action) => {
  visible.value = false
  // 等待离场动画结束 (约 300ms) 后执行回调和销毁
  setTimeout(() => {
    if (action === 'confirm' && props.onConfirm) props.onConfirm()
    if (action === 'cancel' && props.onCancel) props.onCancel()
    if (props.remove) props.remove()
  }, 300)
}
</script>

<template>
  <transition name="fade">
    <div v-show="visible" class="confirm-mask" @click.self="handleClose('cancel')">
      <div class="confirm-box">
        <div class="confirm-header">
          <span class="title">{{ title }}</span>
        </div>
        <div class="confirm-body">
          <p>{{ content }}</p>
        </div>
        <div class="confirm-footer">
          <button class="btn-cancel" @click="handleClose('cancel')">取消</button>
          <button class="btn-confirm" @click="handleClose('confirm')">确定</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.confirm-mask {
  position: fixed;
  inset: 0;
  background-color: rgba(15, 23, 42, 0.2);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.confirm-box {
  width: 340px;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transform: translateY(0) scale(1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.confirm-header {
  padding: 20px 24px 10px;
}

.title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
}

.confirm-body {
  padding: 0 24px 24px;
  font-size: 0.95rem;
  color: #64748b;
  line-height: 1.5;
}

.confirm-footer {
  display: flex;
  border-top: 1px solid #f1f5f9;
  background-color: #f8fafc;
}

.confirm-footer button {
  flex: 1;
  padding: 14px 0;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-cancel {
  color: #64748b;
  border-right: 1px solid #f1f5f9 !important;
}

.btn-cancel:hover { background-color: #f1f5f9; }

.btn-confirm {
  color: #5465ff; /* 使用你的主题色 */
  font-weight: 600 !important;
}

.btn-confirm:hover { background-color: #eef2ff; }

/* 进出场动画 */
.fade-enter-from .confirm-box,
.fade-leave-to .confirm-box {
  transform: translateY(-20px) scale(0.95);
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
</style>