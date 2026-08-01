const ClipboardHelper = {
  copyToClipboard (textToCopy) {
    try {
      // navigator clipboard 需要https等安全上下文
      if (navigator.clipboard && window.isSecureContext) {
        // navigator clipboard 向剪贴板写文本
        navigator.clipboard.writeText(textToCopy)
          .then(() => {
            alert('复制成功')
          })
          .catch(err => {
            alert(err)
          })
      } else {
        // 创建text area
        const textArea = document.createElement('textarea')
        textArea.value = textToCopy
        // 使text area不在viewport，同时设置不可见
        textArea.style.position = 'absolute'
        textArea.style.opacity = 0
        textArea.style.left = '-999999px'
        textArea.style.top = '-999999px'
        document.body.appendChild(textArea)
        textArea.focus()
        textArea.select()
        return new Promise((res, rej) => {
          // 执行复制命令并移除文本框
          document.execCommand('copy') ? res() : rej(new Error('copy failed'))
          textArea.remove()
          alert('复制成功')
        })
      }
    } catch {
      alert('复制失败')
    }
  }
}

export default ClipboardHelper
