/// <reference types="vite/client" />

/**
 * Global declaration for CSS files to prevent TypeScript import errors.
 * This tells the compiler to treat any .css import as a valid side-effect module.
 */
declare module "*.css" {
  const content: any;
  export default content;
}

/**
 * Typing for Vite environment variables.
 */
interface ImportMetaEnv {
  readonly VITE_API_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}