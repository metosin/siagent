(ns app.core
  (:require [app.hiccup :refer [hiccup-demo]]
            [app.interop :refer [interop-demo]]
            [app.reagent :refer [reagent-demo]]
            [reagent.core :as r]
            [uix.core :as uix :refer [defui $]]
            [uix.dom :as dom]))

(defui app []
  ($ uix/strict-mode
     ($ :main
        (r/as-element [hiccup-demo])
        (r/as-element [reagent-demo])
        (r/as-element [interop-demo]))))

(defonce root
  (dom/create-root (js/document.getElementById "app")))

(defn render []
  (dom/render-root ($ app) root))

(defn render-in-compatibility-mode []
  (dom/render ($ app) (js/document.getElementById "app")))

(defn ^:export init []
  ;;(render)
  (render-in-compatibility-mode)
  ,)
