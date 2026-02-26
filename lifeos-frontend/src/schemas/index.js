import { z } from 'zod'

// 记录类型枚举
export const RecordTypeEnum = z.enum(['expense', 'mood', 'event', 'diary'])

// 生活记录 Schema
export const LifeRecordSchema = z.object({
  id: z.number().optional(),
  content: z.string().min(1, '内容不能为空').max(2000, '内容不能超过2000字'),
  recordType: RecordTypeEnum,
  amount: z.number().nullable().optional(),
  tags: z.array(z.string()).default([]),
  emotionScore: z.number().min(-10).max(10).nullable().optional(),
  recordTime: z.string().datetime().optional(),
  createdAt: z.string().datetime().optional(),
  updatedAt: z.string().datetime().optional()
})

// AI 解析结果 Schema
export const AiParseResultSchema = z.object({
  recordTypes: z.array(RecordTypeEnum).min(1, '至少需要一个记录类型'),
  amount: z.number().nullable(),
  tags: z.array(z.string()),
  emotionScore: z.number().min(-10).max(10).nullable(),
  recordTime: z.string().datetime().nullable(),
  summary: z.string()
})

// LLM 配置 Schema
export const LlmConfigSchema = z.object({
  provider: z.string().min(1, '供应商不能为空'),
  model: z.string().min(1, '模型不能为空'),
  apiKey: z.string().min(1, 'API Key 不能为空'),
  baseUrl: z.string().url('请输入有效的 URL').optional().or(z.literal(''))
})

// 主题配置 Schema
export const ThemeConfigSchema = z.object({
  id: z.string(),
  name: z.string(),
  icon: z.string(),
  colors: z.object({
    bgPrimary: z.string(),
    bgSecondary: z.string(),
    bgCard: z.string(),
    textPrimary: z.string(),
    textSecondary: z.string(),
    textMuted: z.string(),
    borderColor: z.string(),
    inputBg: z.string(),
    inputBorder: z.string(),
    shadowColor: z.string(),
    accentColor: z.string(),
    primaryColor: z.string(),
    gradientFrom: z.string(),
    gradientTo: z.string()
  })
})

// 查询参数 Schema
export const QueryParamsSchema = z.object({
  startDate: z.string().date().optional(),
  endDate: z.string().date().optional(),
  type: RecordTypeEnum.optional(),
  tag: z.string().optional()
})

// 用户输入 Schema（用于验证用户输入的文本）
export const UserInputSchema = z.object({
  text: z.string().min(1, '请输入内容').max(2000, '内容不能超过2000字')
})

// 导出类型
export type LifeRecord = z.infer<typeof LifeRecordSchema>
export type AiParseResult = z.infer<typeof AiParseResultSchema>
export type LlmConfig = z.infer<typeof LlmConfigSchema>
export type ThemeConfig = z.infer<typeof ThemeConfigSchema>
export type QueryParams = z.infer<typeof QueryParamsSchema>
export type UserInput = z.infer<typeof UserInputSchema>

// 验证函数
export function validateLifeRecord(data: unknown) {
  return LifeRecordSchema.safeParse(data)
}

export function validateAiParseResult(data: unknown) {
  return AiParseResultSchema.safeParse(data)
}

export function validateLlmConfig(data: unknown) {
  return LlmConfigSchema.safeParse(data)
}

export function validateUserInput(data: unknown) {
  return UserInputSchema.safeParse(data)
}

export function validateQueryParams(data: unknown) {
  return QueryParamsSchema.safeParse(data)
}
