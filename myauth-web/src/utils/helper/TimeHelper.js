function addTenZero (num) {
  return num < 10 ? ('0' + num) : ('' + num)
}
const TimeHelper = {
  getFormatDate (nS) {
    if (!nS) return '-'
    if (parseInt(nS) == -1) return '永久'
    const date = new Date(parseInt(nS) * 1000)
    const formatDate = (now) => {
      const year = now.getFullYear()
      const month = now.getMonth() + 1
      const date = now.getDate()
      const hour = now.getHours()
      const minute = now.getMinutes()
      const second = now.getSeconds()
      return year + '-' + addTenZero(month) + '-' + addTenZero(date) + ' ' + addTenZero(hour) + ':' + addTenZero(minute) + ':' + addTenZero(second)
    }
    return formatDate(date)
  }
}

export default TimeHelper
