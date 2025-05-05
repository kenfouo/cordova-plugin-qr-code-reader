import Foundation
import UIKit
import Vision
import Cordova

@objc(QRReader) class QRReader: CDVPlugin {

    @objc(decodeImage:)
    func decodeImage(command: CDVInvokedUrlCommand) {
        guard let base64String = command.arguments[0] as? String else {
            self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "Paramètre base64 manquant"), callbackId: command.callbackId)
            return
        }

        // Convertir le base64 en UIImage
        guard let imageData = Data(base64Encoded: base64String, options: .ignoreUnknownCharacters),
              let image = UIImage(data: imageData),
              let cgImage = image.cgImage else {
            self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "Image non valide"), callbackId: command.callbackId)
            return
        }

        let request = VNDetectBarcodesRequest { request, error in
            if let error = error {
                self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "Erreur : \(error.localizedDescription)"), callbackId: command.callbackId)
                return
            }

            if let results = request.results, let barcode = results.first as? VNBarcodeObservation, let payload = barcode.payloadStringValue {
                self.commandDelegate.send(CDVPluginResult(status: .ok, messageAs: payload), callbackId: command.callbackId)
            } else {
                self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "Aucun QR code détecté."), callbackId: command.callbackId)
            }
        }

        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        do {
            try handler.perform([request])
        } catch {
            self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "Erreur traitement image : \(error.localizedDescription)"), callbackId: command.callbackId)
        }
    }
}
