#import "QRReader.h"
#import <CoreImage/CoreImage.h>
#import <UIKit/UIKit.h>

@implementation QRReader

- (void)decodeImage:(CDVInvokedUrlCommand*)command {
    NSString* base64String = [command.arguments objectAtIndex:0];

    if (!base64String) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Paramètre base64 manquant"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }

    NSData* imageData = [[NSData alloc] initWithBase64EncodedString:base64String options:NSDataBase64DecodingIgnoreUnknownCharacters];
    UIImage* image = [UIImage imageWithData:imageData];

    if (!image) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Image non valide"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }

    CIImage* ciImage = [[CIImage alloc] initWithImage:image];
    CIDetector* detector = [CIDetector detectorOfType:CIDetectorTypeQRCode
                                              context:nil
                                              options:@{CIDetectorAccuracy:CIDetectorAccuracyHigh}];
    NSArray* features = [detector featuresInImage:ciImage];

    if (features.count > 0) {
        CIQRCodeFeature* feature = [features objectAtIndex:0];
        NSString* message = feature.messageString;

        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } else {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Aucun QR code détecté"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
}

@end
