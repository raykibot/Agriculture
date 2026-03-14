<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  message: { type: String, required: true },
  type: { type: String, default: 'info' }, // success, error, warning, info
  duration: { type: Number, default: 2500 },
  onDestroy: { type: Function }
})

const visible = ref(false)

onMounted(() => {
  visible.value = true
  setTimeout(() => {
    visible.value = false
  }, props.duration)
})

// 动画结束后的回调，通知外部销毁 DOM
const handleAfterLeave = () => {
  if (props.onDestroy) {
    props.onDestroy()
  }
}
</script>

<template>
  <transition name="message-fade" @after-leave="handleAfterLeave">
    <div v-show="visible" :class="['elegant-message', type]">
      
      <svg v-if="type === 'success'" class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5 13l4 4L19 7" />
      </svg>
      
      <svg v-if="type === 'error'" class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M6 18L18 6M6 6l12 12" />
      </svg>
      
      <svg v-if="type === 'warning'" class="msg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>

      <span class="msg-text">{{ message }}</span>
    </div>
  </transition>
</template>

<style scoped>
/* 高级感的核心在于：小巧的内边距、极淡的边框、柔和且范围较大的阴影 */
.elegant-message {
  position: fixed;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #ffffff;
  border: 1px solid #f1f5f9;
  border-radius: 6px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  z-index: 9999;
  min-width: 200px;
  justify-content: center;
}

.msg-icon {
  width: 18px;
  height: 18px;
  margin-right: 8px;
}

.msg-text {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 状态配色 (摒弃高饱和，采用莫兰迪低饱和色系) */
.success .msg-icon { color: #10b981; }
.error .msg-icon { color: #ef4444; }
.warning .msg-icon { color: #f59e0b; }

/* 丝滑的进入/退出动画 */
.message-fade-enter-active,
.message-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.message-fade-enter-from,
.message-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px); /* 向上偏移淡出 */
}
</style>