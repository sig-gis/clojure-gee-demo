(ns clojure-gee-demo.image-examples
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python  :refer [py.- py. py* py** py..] :as py]))

;; Search for layers in the Earth Engine Data Catalog
;; https://developers.google.com/earth-engine/datasets
;;
;; To generate Raster or Vector datasets as GeoTIFFs or SHPs, use the ee/Export functions
;; https://developers.google.com/earth-engine/guides/exporting

;; Try installing and importing python-folium for embedded leaflet.js maps
;; https://python-visualization.github.io/folium/

;;=======================================
;; Load Python earthengine-api library
;;=======================================

(require-python '[ee :bind-ns])

;;=======================================
;; Initialize earthengine-api library
;;=======================================

(defonce ee-initialized? (atom false))

(when-not @ee-initialized?
  (ee/Initialize)
  (reset! ee-initialized? true))

;;=======================================
;; Fetch an image
;;=======================================

(defn fetch-landsat-image []
  (let [image (ee/Image "LANDSAT/LT05/C01/T1_SR/LT05_034033_20000913")]
    {:image-request  image
     :image-response (py. image getInfo)}))

#_(fetch-landsat-image)

;;=======================================
;; Reduce an image
;;=======================================

(defn reduce-landsat-image []
  (let [image                (ee/Image "LANDSAT/LE7_TOA_5YEAR/2008_2012")
        sierra-nevada-filter (py.. ee -Filter (eq "us_l3name" "Sierra Nevada"))
        region               (ee/Feature (py.. (ee/FeatureCollection "EPA/Ecoregions/2013/L3")
                                               (filter sierra-nevada-filter)
                                               (first)))
        mean-dictionary      (py** image reduceRegion {:reducer   (py.. ee -Reducer mean)
                                                       :geometry  (py. region geometry)
                                                       :scale     30
                                                       :maxPixels 1e9})]
    (py. mean-dictionary getInfo)))

#_(reduce-landsat-image)

;;=======================================
;; Reduce an image collection
;;=======================================

(defn reduce-sentinel-image-collection []
  (let [point              (py.. ee -Geometry (Point [-81.31 29.90]))
        collection         (py.. (ee/ImageCollection "COPERNICUS/S2")
                                 (filterDate "2016-01-01" "2016-12-31")
                                 (filterBounds point))
        min-max-reducer    (py.. ee -Reducer minMax)
        reduced-collection (py. collection reduce min-max-reducer)]
    (py. reduced-collection getInfo)))

#_(reduce-sentinel-image-collection)
