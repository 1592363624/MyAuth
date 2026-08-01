module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    es6: true
  },
  extends: [
    'plugin:vue/strongly-recommended',
    '@vue/standard'
  ],
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@babel/eslint-parser',
    requireConfigFile: false,
    sourceType: 'module'
  },
  rules: {
    'generator-star-spacing': 'off',
    'no-mixed-operators': 0,
    'vue/max-attributes-per-line': [
      2,
      {
        singleline: 5,
        multiline: {
          max: 1
        }
      }
    ],
    'vue/attribute-hyphenation': 0,
    'vue/html-self-closing': 0,
    'vue/component-name-in-template-casing': 0,
    'vue/html-closing-bracket-spacing': 0,
    'vue/singleline-html-element-content-newline': 0,
    'vue/no-unused-components': 0,
    'vue/multiline-html-element-content-newline': 0,
    'vue/no-use-v-if-with-v-for': 0,
    'vue/html-closing-bracket-newline': 0,
    'vue/no-parsing-error': 0,
    'no-console': 0,
    'no-tabs': 0,
    quotes: [
      2,
      'single',
      {
        avoidEscape: true,
        allowTemplateLiterals: true
      }
    ],
    semi: [
      2,
      'never',
      {
        beforeStatementContinuationChars: 'never'
      }
    ],
    'no-delete-var': 2,
    'prefer-const': [
      2,
      {
        ignoreReadBeforeAssign: false
      }
    ],
    'template-curly-spacing': 'off',
    indent: 'off',
    // 以下规则为 ESLint 8 / eslint-plugin-vue 9 新增，项目历史代码未遵循，暂降为 warn
    'vue/multi-word-component-names': 'off',
    eqeqeq: 'off',
    'no-throw-literal': 'warn',
    'promise/param-names': 'warn',
    'vue/no-mutating-props': 'warn',
    'vue/require-prop-type-constructor': 'warn',
    'no-new-func': 'warn'
  }
}
