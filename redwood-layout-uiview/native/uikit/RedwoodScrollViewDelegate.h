#import <CoreGraphics/CoreGraphics.h>
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@protocol RedwoodScrollViewDelegate <NSObject>

 @property(nonatomic, readonly) CGSize intrinsicContentSize;
 - (CGSize)sizeThatFits:(CGSize)size;
 - (void)setNeedsLayout;
 - (void)layoutSubviews;

@end
