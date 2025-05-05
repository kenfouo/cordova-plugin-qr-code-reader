#import <Cordova/CDVPlugin.h>

@interface QRReader : CDVPlugin

- (void)decodeImage:(CDVInvokedUrlCommand*)command;

@end