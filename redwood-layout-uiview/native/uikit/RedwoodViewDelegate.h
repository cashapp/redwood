#import <CoreGraphics/CoreGraphics.h>
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@protocol RedwoodViewDelegate <NSObject>

 @property(nonatomic, readonly) CGSize intrinsicContentSize;

@end
