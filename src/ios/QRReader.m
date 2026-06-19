#import "QRReader.h"
#import <Vision/Vision.h>
#import <UIKit/UIKit.h>

@implementation QRReader

- (void)decodeImage:(CDVInvokedUrlCommand*)command {

    NSString* base64String = [command.arguments objectAtIndex:0];

    if (!base64String) {
        CDVPluginResult* result =
        [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Paramètre base64 manquant"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }

    NSData* imageData = [[NSData alloc] initWithBase64EncodedString:base64String
                                                            options:NSDataBase64DecodingIgnoreUnknownCharacters];

    UIImage* image = [UIImage imageWithData:imageData];

    if (!image) {
        CDVPluginResult* result =
        [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Image non valide"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }

    NSArray *rotations = @[@0,@90,@180,@270];

    for (NSNumber *angle in rotations) {

        UIImage *rotatedImage = [self rotateImage:image degrees:[angle floatValue]];
        NSString *decoded = [self scanBarcode:rotatedImage];

        if (decoded != nil) {

            CDVPluginResult* result =
            [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:decoded];

            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            return;
        }
    }

    CDVPluginResult* result =
    [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Aucun code détecté"];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

#pragma mark - Scan barcode

- (NSString*)scanBarcode:(UIImage*)image {

    CIImage *ciImage = [[CIImage alloc] initWithImage:image];

    VNDetectBarcodesRequest *request = [[VNDetectBarcodesRequest alloc] init];

    VNImageRequestHandler *handler =
    [[VNImageRequestHandler alloc] initWithCIImage:ciImage options:@{}];

    NSError *error = nil;

    [handler performRequests:@[request] error:&error];

    if (error) return nil;

    NSArray *results = request.results;

    if (results.count == 0) return nil;

    VNBarcodeObservation *barcode = results.firstObject;

    NSString *value = barcode.payloadStringValue ?: @"";
    NSString *format = barcode.symbology ?: @"UNKNOWN";

    return [NSString stringWithFormat:@"%@:::%@:::false:::Images", value, format];
}

#pragma mark - Rotation image

- (UIImage *)rotateImage:(UIImage *)image degrees:(CGFloat)degrees {

    CGSize size = image.size;

    UIGraphicsBeginImageContext(size);
    CGContextRef context = UIGraphicsGetCurrentContext();

    CGContextTranslateCTM(context, size.width/2, size.height/2);
    CGContextRotateCTM(context, degrees * M_PI / 180);
    CGContextScaleCTM(context, 1.0, -1.0);

    CGContextDrawImage(context,
                       CGRectMake(-size.width/2, -size.height/2, size.width, size.height),
                       image.CGImage);

    UIImage *rotatedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    return rotatedImage;
}

@end

