import i18n from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import { initReactI18next } from 'react-i18next';

import pt from './locales/pt.json';
import en from './locales/en.json';

const supportedLngs = ['pt', 'en'];

const normalizeLanguage = (lng) => {
  if (!lng) return 'pt';

  const lower = lng.toLowerCase();
  if (lower.startsWith('en')) {
    return 'en';
  }

  if (lower.startsWith('pt')) {
    return 'pt';
  }

  return 'pt';
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      pt: { translation: pt },
      en: { translation: en },
    },
    fallbackLng: 'pt',
    supportedLngs,
    load: 'currentOnly',
    interpolation: {
      escapeValue: false,
    },
    detection: {
      order: ['localStorage', 'navigator', 'htmlTag'],
      caches: ['localStorage'],
      lookupLocalStorage: 'taskSchedulerLocale',
      convertDetectedLanguage: normalizeLanguage,
    },
  });

export const getAppLocale = () => normalizeLanguage(i18n.resolvedLanguage || i18n.language);

export default i18n;
