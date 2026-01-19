# SW Ad Server API 文档

## 概述

SW Ad Server 是一个基于 TypeScript/Node.js 的后端服务，提供用户注册管理、广告服务和区块链资产查询功能。所有API都使用统一的响应格式和错误处理机制。

## 通用响应格式

所有API接口都返回以下格式的响应：

```typescript
interface ApiResponse<T = any> {
  success: boolean; // 是否成功
  data?: T; // 响应数据
  message?: string; // 错误消息
  error?: string; // 错误类型
}
```

### 错误类型

- `BusinessError`: 业务逻辑错误（400状态码）
- `InvalidArgumentError`: 参数验证错误（400状态码）
- `UnknownError`: 未知错误（500状态码）

## 鉴权机制

### 需要鉴权的接口

- 所有接口默认需要鉴权，除非特别说明
- 鉴权通过请求头中的签名验证实现

### 鉴权请求头

```typescript
type headers = {
  'user-id': string; // 用户ID，长度64的16进制字符串，不含0x前缀，eg: 49e729012ea91d45df32385b29d453e039932f9a607a87226206328d59c92e87
  'request-time': string; // 请求时间戳，单位为毫秒
  'request-sign': string; // 请求签名，以太坊签名标准格式，以0x开头的长度为132的16进制字符串，eg: 0x0ba190a731ca3a13d281838ad37c63c9c546075a6b948cff9ce0dd550711e5504ba5f9ad36be0fabb7cb5ede17adfca8b77f3cb556c59464cd2744175623a7471b
};
```

### 签名过程

签名对象：

```typescript
type Obj2Sign = {
  userId: string; // eg: 'Alice'
  timestamp: string; // eg: '1763118631678'
  url: string; // eg: '/api/user/register'
  method: 'GET' | 'POST';
  body?: any;
  query?: any;
};
```

1. 对签名对象按 key 升序排列后转为 json 字符串
2. 计算字符串的SHA-256哈希
3. 使用主密钥对哈希值签名

## API接口列表

### 1. 健康检查接口

**接口路径**: `GET /api/health`

**鉴权**: 无需鉴权

**功能描述**: 检查服务是否正常运行

**请求参数**: 无

**响应示例**:

```json
{
  "success": true,
  "message": "SW Ad Server is running",
  "timestamp": "2025-01-01T00:00:00.000Z"
}
```

---

### 2. 注册相关接口

#### 2.1 预注册接口

**接口路径**: `POST /api/user/pre-register`

**鉴权**: 无需鉴权

**功能描述**: 注册账号前申请相关资源，生成待签名的注册信息

**请求参数**:

```typescript
interface request {
  masterPK: string; // 主密钥的未压缩公钥，以0x04开头的长度为132的16进制字符串
  chatPK: string; // 聊天密钥的未压缩公钥，以0x04开头的长度为132的16进制字符串
  nickname: string; // 昵称，15个字符以内
}
```

**响应数据**:

```typescript
interface response {
  registerPayload: string; // 待签名的注册信息，JSON字符串
}
```

#### 2.2 正式注册接口

**接口路径**: `POST /api/user/register`

**鉴权**: 无需鉴权

**功能描述**: 注册账号并绑定硬件设备

**请求参数**:

```typescript
interface request {
  registerPayload: string; // 预注册接口返回的待签名注册信息
  signature: string; // 使用主密钥对registerPayload的SHA-256值进行签名，以太坊签名标准格式
  devicePK: string; // 硬件设备的公钥，以0x04开头的长度为132的16进制字符串
  deviceChip: string; // 硬件设备的芯片ID，原始数据长度16bytes，编码方式不确定
}
```

**响应数据**:

```typescript
interface response {
  userId: string; // 用户ID，长度为64的16进制字符串，不含0x前缀，eg: 49e729012ea91d45df32385b29d453e039932f9a607a87226206328d59c92e87
}
```

---

### 3. Ads3广告服务接口

#### 3.1 获取广告列表

**接口路径**: `GET /api/ads3/ads`

**鉴权**: 需要鉴权

**功能描述**: 获取用户的广告列表

**请求参数**:

无

**响应数据**:

```typescript
interface response {
  ads: Array<Ads3Ad>; // 广告列表
}
interface Ads3Ad {
  icon: string; // 广告图标URL
  adBlockId: string; // 广告位ID
  adId: string; // 广告ID
  campaignId: string; // 活动ID
  image: string; // 广告图片URL
  text: string; // 广告文本
  destination: {
    // 广告目标
    actionType: string; // 动作类型（如'visit.website'）
    url: string; // 目标URL
  };
}
```

#### 3.2 上报广告点击事件

**接口路径**: `POST /api/ads3/click`

**鉴权**: 需要鉴权

**功能描述**: 上报用户点击广告事件

**请求参数**:

```typescript
interface request {
  adId: string; // 广告ID
  campaignId: string; // 活动ID
}
```

**响应数据**:

```typescript
interface response {
  success: boolean; // 是否上报成功
}
```

---

### 4. Alchemy资产查询接口

#### 4.1 获取代币资产

**接口路径**: `POST /api/assets/tokens`

**鉴权**: 需要鉴权

**功能描述**: 查询用户在不同区块链网络上的代币资产，包含历史价格数据

**请求参数**:

```typescript
interface request {
  addresses: Array<{
    address: string; // 钱包地址
    networks: Network[]; // 网络列表
  }>;
  withMetadata?: boolean; // 是否包含元数据，默认 true
  withPrices?: boolean; // 是否包含价格信息，默认 true
  includeNativeTokens?: boolean; // 是否包含原生代币，默认 true
  includeErc20Tokens?: boolean; // 是否包含ERC20代币，默认 true
  pageKey?: string; // 分页键
}
export type Network =
  | 'eth-mainnet'
  | 'eth-goerli'
  | 'polygon-mainnet'
  | 'polygon-mumbai'
  | 'arbitrum-mainnet'
  | 'optimism-mainnet'
  | 'base-mainnet'
  | 'solana-mainnet';
```

**响应数据**:

```typescript
interface response {
  tokens: Token[]; // 代币列表
  pageKey?: string; // 分页键
}
interface Token {
  address: string; // 钱包地址
  network: Network; // 网络类型
  tokenAddress: string; // 代币合约地址
  tokenBalance: string; // 代币余额
  tokenMetadata?: {
    // 代币元数据
    name?: string; // 代币名称
    symbol?: string; // 代币符号
    decimals?: number; // 代币精度
    logo?: string; // 代币logo
  };
  tokenPrices?: TokenPrice[]; // 历史价格数据
  error?: string; // 错误信息
}

interface TokenPrice {
  currency: string; // 货币类型
  value: string; // 价格值
  lastUpdatedAt: string; // 最后更新时间
}
```

#### 4.2 获取NFT资产

**接口路径**: `POST /api/assets/nfts`

**鉴权**: 需要鉴权

**功能描述**: 查询用户在不同区块链网络上的NFT资产，包含地板价信息

**请求参数**:

```typescript
interface request {
  addresses: Array<{
    address: string; // 钱包地址
    networks: Network[]; // 网络列表
  }>;
  withMetadata?: boolean; // 是否包含元数据，默认 true
  pageKey?: string; // 分页键
  pageSize?: number; // 页面大小，默认 100
  orderBy?: 'name' | 'lastTransferTime' | 'mintTime'; // 排序字段
  sortOrder?: 'asc' | 'desc'; // 排序方向
}
```

**响应数据**:

```typescript
interface response {
  ownedNfts: Nft[]; // NFT列表
  totalCount: number; // 总数
  pageKey?: string; // 分页键
}
interface Nft {
  address: string; // 钱包地址
  network: Network; // 网络类型
  contractAddress: string; // 合约地址
  tokenId: string; // Token ID
  balance: string; // NFT数量
  isSpam: boolean; // 是否是垃圾NFT
  spamClassifications: string[]; // 垃圾分类
  nftMetadata?: {
    // NFT元数据
    name?: string; // NFT名称
    description?: string; // NFT描述
    image?: string; // NFT图片
    attributes?: Array<{
      // 属性
      trait_type: string;
      value: string;
    }>;
  };
  floorPrices?: {
    // 地板价信息（仅限eth-mainnet）
    [marketplaceName: string]: {
      floorPrice?: number; // 地板价
      priceCurrency?: 'ETH'; // 价格货币
      collectionUrl?: string; // 集合URL
      retrievedAt?: string; // 获取时间
      error?: string; // 错误信息
    };
  };
  error?: string; // 错误信息
}
```
