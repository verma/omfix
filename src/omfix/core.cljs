(ns ^:figwheel-always omfix.core
    (:require [om.core :as om]
              [om.dom :as dom]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def state (atom {:timer {:value 0}}))

(def root (om/root-cursor state))
(def timer (om/ref-cursor (:timer root)))

(defn- random-color []
  (let [angle (rand-int 360)]
    (str "hsl(" angle ",50%,50%)")))

(defn child-comp [_ owner]
  (reify
    om/IInitState
    (init-state [_]
      {:local-timer 0})

    om/IDidMount
    (did-mount [_]
      (js/setInterval
       (fn []
         (om/update-state! owner #(update-in % [:local-timer] inc)))
       1000))
    
    om/IRenderState
    (render-state [_ {:keys [local-timer]}]
      (let [t (om/observe owner timer)]
        (dom/h2 #js {:style #js {:color (random-color)}}
                (str "child timer:"  (:value @t) "." local-timer))))))

(defn main-comp [_ owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (js/setInterval (fn []
                      (om/transact! timer #(update-in % [:value] inc)))
                    5000))
    
    om/IRender
    (render [_]
      (let [t (om/observe owner timer)]
        (dom/div #js {:style #js {:color (random-color)}}
                 (dom/h1 #js {}
                         (str "timer is:" (:value @t)))

                 (om/build child-comp {}))))))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(om/root main-comp state {:target (.getElementById js/document "app")})

