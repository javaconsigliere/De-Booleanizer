#  Ze-DeBooleanizer
![Logo](https://xlogistx.io/articles/de-booleanizer/ZeDebooleanizer.JPG)

DeBouleanizer is a java Application that uses ChatGPT API to analysis images and process voice tokens.

## How to use it

Make sure you have jre 11+ installed on your system.\
Get [jar-loader.jar](https://xlogistx.io/apps/jar-loader.jar)\
Get [de-booleanizer.jar](https://xlogistx.io/apps/de-booleanizer.jar)

Then type\
java -jar [jar-loader.jar](https://xlogistx.io/apps/jar-loader.jar) -jar [de-booleanizer.jar](https://xlogistx.io/apps/de-booleanizer.jar) cap=yes json-filter=**filter.json** json-ai-config=**ai-config.json**  gpt-key=**[chat gtp api key]**

**filter.json**
```json
{
  "name": "java-between",
  "description": "Get text between prefix{}postfix",
  "type": "BETWEEN",
  "extension" : "java",
  "prefix": "```code",
  "postfix": "```"
}
```


**ai-config.json**
```json
{
  "prompts": [
    "Analyze the image and generate an accurate prompt that will be consumed by an LLM",
    "Analyse the image and provide an optimized solution. Provide code solution between ```code and ``` "

  ],
  "models": [
    "gpt-5-nano,gpt-5",
    "gpt-5-nano",
    "gpt-5",
    "gpt-5-mini"
  ]
}
```

