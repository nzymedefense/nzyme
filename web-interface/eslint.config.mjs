import js from "@eslint/js";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";

export default [
  js.configs.recommended,
  react.configs.flat.recommended,
  react.configs.flat["jsx-runtime"],
  {
    plugins: {
      "react-hooks": reactHooks,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
    },
  },
  {
    languageOptions: {
      ecmaVersion: 2021,
      sourceType: "module",
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
      },
    },
    settings: {
      react: {
        version: "detect",
      },
    },
    rules: {
      indent: "off",
      "padded-blocks": "off",
      "react/prop-types": "off",
      "react/no-deprecated": "off",
      quotes: "off",
      semi: "off",
      "object-curly-spacing": "off",
      "space-before-function-paren": "off",
      "eol-last": "off",
      "no-trailing-spaces": "off",
      "react/jsx-no-target-blank": "off",
      "multiline-ternary": "off",
      "dot-notation": "off",
      "space-infix-ops": "off",
      "key-spacing": "off",
      "operator-linebreak": "off",
      "no-unneeded-ternary": "off",
    },
  },
];
