const fs = require('fs');
const path = require('path');

const target = path.join(__dirname, 'node_modules', 'rollup', 'dist', 'native.js');
if (fs.existsSync(target)) {
  const content = `const wasm = require('@rollup/wasm-node/dist/native.js');

exports.parse = wasm.parse;
exports.parseAsync = wasm.parseAsync;
exports.xxhashBase64Url = wasm.xxhashBase64Url;
exports.xxhashBase36 = wasm.xxhashBase36;
exports.xxhashBase16 = wasm.xxhashBase16;
exports.flushLlvmCoverage = wasm.flushLlvmCoverage;
`;
  fs.writeFileSync(target, content, 'utf8');
  console.log('[patch-rollup] Successfully configured Rollup to use pure WebAssembly.');
}
