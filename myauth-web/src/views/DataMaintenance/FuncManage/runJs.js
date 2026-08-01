// 注册filter
const filters = {}
export const resolveFilter = (id) => {
  if (typeof id !== 'string') {
    return id
  } else {
    return filters[id]// 返回一个函数
  }
}

// 创建实例对象
export function createExpInstance (paramsEntity) {
  for (const key in paramsEntity) {
    this[key] = paramsEntity[key]
  }
}
createExpInstance.prototype._f = resolveFilter
createExpInstance.prototype.getValue = function (exp) {
  const code = `return (()=>{with (this) {return ${exp}}})()`
  try {
    const func = new Function(code)
    const currentFunc = func.bind(this)// 绑定函数this指向，严格模式下this为undefined，非严格模式为window
    return currentFunc()
    // return func();
  } catch (err) {
    // console.log(err)
  }
}

// let expInstance = new createExpInstance(paramsEntity);
// let expValue = expInstance.getValue(expStr);
