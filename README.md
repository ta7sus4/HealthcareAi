# repAIr  (ヘルスケアAI)
## 概要
AIを使ってユーザの健康を手助けするアプリ

## 機能一覧
- 対話機能
  - ユーザの入力内容から対話形式でAIが問題を解決する

- 健康度の数値化
  - AIが質問を考え、ユーザの回答をもとに健康度を数値化、アドバイスを行う

|対話機能|健康度の数値化|
|--|--|
| <img src="https://github.com/ta7sus4/HealthcareAi/assets/85665552/1fdb4596-e1f7-48b6-9cd3-91ca7984aac8" width=350> | <img src="https://github.com/ta7sus4/HealthcareAi/assets/85665552/2cea398a-21fd-4b29-aaf2-4127c3b7642b" width=350> |

## 使用技術
- Kotlin
- Jetpack Compose
- Kotlin Coroutines
- Jetpack Room

## 環境構築
APIキーの追加

`local.properties`に以下を追記
```
api_key="ここにキーを記載"
```


## ブランチ名
`feature/`: 機能の追加

`fix/`: バグ修正・細かい修正
